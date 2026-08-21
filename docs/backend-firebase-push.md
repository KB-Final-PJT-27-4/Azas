# Backend Firebase Cloud Messaging setup

The backend uses the Firebase Admin Java SDK only when the `firebase` Spring
profile is active. The default profile uses `FakePushMessageSender`, so local
development and unit tests do not require Firebase credentials or send external
messages.

## Firebase service account

In Firebase Console, open **Project settings > Service accounts** and generate
a private key for the backend service account. Store the downloaded JSON file
outside this repository. Never commit or copy its contents into an environment
file.

Before starting Tomcat, configure Application Default Credentials:

```bash
export SPRING_PROFILES_ACTIVE=firebase
export GOOGLE_APPLICATION_CREDENTIALS="/absolute/path/firebase-service-account.json"
export FIREBASE_PROJECT_ID="your-firebase-project-id"
```

`FIREBASE_PROJECT_ID` is optional when the project can be inferred from the
service-account JSON, but setting it explicitly helps prevent sending through
the wrong Firebase project.

The existing token encryption key is also required:

```bash
export PUSH_TOKEN_ENCRYPTION_KEY_BASE64="<32-byte Base64 key>"
```

Restart Tomcat after changing any environment variable. The environment of an
already running Tomcat process does not change.

## Delivery behavior

`MemberPushService` loads every active device for a member, decrypts each FCM
registration token, and sends the message through `PushMessageSender`.

- `UNREGISTERED` and `SENDER_ID_MISMATCH` deactivate the device immediately.
- `INTERNAL`, `UNAVAILABLE`, and `QUOTA_EXCEEDED` are marked retryable by the
  Firebase gateway. Persistent retry storage is added in the next feature.
- Invalid stored ciphertext also deactivates the affected device.
