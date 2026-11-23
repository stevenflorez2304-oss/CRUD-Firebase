const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProductPayload(id, overrides = {}) {
  return {
    id,
    name: "Nike Air Max 90 Essential",
    description: "Tenis Nike clasicos talla 42",
    priceCop: 589000,
    quantity: 14,
    updatedAt: Date.now() - 2000,
    serverUpdatedAt: new Date(Date.now() - 1000),
    isDeleted: false,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot list, read, write, or delete products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const productId = "prod-nike-1";

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").doc(productId).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("products").doc(productId).set(validProductPayload(productId))
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").doc(productId).delete());
});

test("Authenticated owner can create, read, list, update (including soft-delete tombstone), and delete own product", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const productId = "prod-nb-550";
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("products").doc(productId);

  await assertSucceeds(docRef.set(validProductPayload(productId)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("products").get());

  // Full set() update mirroring FirestoreProductDataSource.push()
  await assertSucceeds(
    docRef.set(
      validProductPayload(productId, {
        quantity: 10,
        priceCop: 549000,
        serverUpdatedAt: new Date(Date.now() - 500),
      })
    )
  );

  // Soft-delete tombstone sync
  await assertSucceeds(
    docRef.set(
      validProductPayload(productId, {
        isDeleted: true,
        serverUpdatedAt: new Date(Date.now() - 300),
      })
    )
  );

  await assertSucceeds(docRef.delete());
});

test("Cross-user isolation prevents Bob from reading or writing Alice's products", async () => {
  const productId = "prod-adidas-samba";
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("products")
      .doc(productId)
      .set(validProductPayload(productId));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceCol = bobDb.collection("users").doc(ALICE_UID).collection("products");

  await assertFails(aliceCol.get());
  await assertFails(aliceCol.doc(productId).get());
  await assertFails(aliceCol.doc(productId).set(validProductPayload(productId, { name: "Hijacked" })));
  await assertFails(aliceCol.doc(productId).delete());
});

test("Validation rejects invalid payloads, ghost fields, future timestamps, and mismatched IDs", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("products");

  // Empty name
  await assertFails(col.doc("p1").set(validProductPayload("p1", { name: "" })));
  // Negative price
  await assertFails(col.doc("p2").set(validProductPayload("p2", { priceCop: 0 })));
  // Negative quantity
  await assertFails(col.doc("p3").set(validProductPayload("p3", { quantity: -1 })));
  // Mismatched document id
  await assertFails(col.doc("p4").set(validProductPayload("other-id")));
  // Ghost field
  await assertFails(col.doc("p5").set(validProductPayload("p5", { adminOverride: true })));
  // Future serverUpdatedAt timestamp
  await assertFails(col.doc("p6").set(validProductPayload("p6", { serverUpdatedAt: new Date(Date.now() + 60000) })));
});
