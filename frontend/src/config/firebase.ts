import { getApp, getApps, initializeApp, type FirebaseApp } from 'firebase/app'
import { getMessaging, isSupported, type Messaging } from 'firebase/messaging'

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

// pushNotifications.ts에서 사용하는 VAPID Key
export const firebaseVapidKey =
  import.meta.env.VITE_FIREBASE_VAPID_KEY ?? ''

// Firebase 푸시 설정 여부
export const isFirebaseMessagingConfigured = Boolean(
  firebaseConfig.apiKey &&
  firebaseConfig.authDomain &&
  firebaseConfig.projectId &&
  firebaseConfig.messagingSenderId &&
  firebaseConfig.appId &&
  firebaseVapidKey,
)

// Firebase 앱을 반환하는 함수
export const getFirebaseApp = (): FirebaseApp => {
  return getApps().length > 0 ? getApp() : initializeApp(firebaseConfig)
}

// 기존 코드에서 이 함수를 사용한다면 유지
export const getFirebaseMessaging =
  async (): Promise<Messaging | null> => {
    if (!isFirebaseMessagingConfigured) {
      return null
    }

    const supported = await isSupported()

    if (!supported) {
      return null
    }

    return getMessaging(getFirebaseApp())
  }
