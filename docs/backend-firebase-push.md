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

Business services publish a push request only when the matching database
notification was inserted. The listener delivers it after the surrounding
transaction commits, so Firebase outages do not roll back mission or transfer
operations. The currently connected events are mission assignment, submission,
approval, rejection and cancellation, plus auto-transfer retry success/failure.

## Frontend web push

The frontend needs all `VITE_FIREBASE_*` values from the same Firebase project
as the backend service account. It registers `/firebase-messaging-sw.js`, obtains
an FCM token with the public VAPID key, and stores the device through
`POST /api/v1/push-devices`.

The service worker displays browser notifications in both foreground and
background states. A visible application window also receives a message for an
in-app toast. Notification clicks accept same-origin relative paths only, focus
an existing application window when possible, and otherwise open a new window.

After changing Vite environment variables or the service worker, rebuild and
redeploy the frontend. Test with Chrome or Edge over HTTPS (localhost is also a
secure context):

1. Log in and enable browser notifications in My Page > Notification Settings.
2. Confirm `POST /api/v1/push-devices` returns `201` or `200`.
3. Trigger a connected mission or auto-transfer retry event for that member.
4. Verify foreground, background, and closed-window delivery.
5. Click the browser notification and verify the expected in-app route opens.
