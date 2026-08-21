import {
  getApp,
  getApps,
  initializeApp,
  type FirebaseApp,
  type FirebaseOptions,
} from 'firebase/app'

const firebaseConfig: FirebaseOptions = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

const requiredConfigValues = [
  firebaseConfig.apiKey,
  firebaseConfig.authDomain,
  firebaseConfig.projectId,
  firebaseConfig.messagingSenderId,
  firebaseConfig.appId,
  import.meta.env.VITE_FIREBASE_VAPID_KEY,
]

export const isFirebaseMessagingConfigured = requiredConfigValues.every(
  (value) => typeof value === 'string' && value.trim().length > 0,
)

export const firebaseVapidKey = import.meta.env.VITE_FIREBASE_VAPID_KEY ?? ''

export const getFirebaseApp = (): FirebaseApp => {
  if (!isFirebaseMessagingConfigured) {
    throw new Error('Firebase Web Push 환경변수가 설정되지 않았습니다.')
  }

  return getApps().length > 0 ? getApp() : initializeApp(firebaseConfig)
}
