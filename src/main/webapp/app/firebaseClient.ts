// Minimal Firebase helper for getting an FCM token from the browser
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import { toast } from 'react-toastify';

// Firebase config provided by the user
const firebaseConfig = {
  apiKey: 'AIzaSyAi5BxuPTP8OPtCtJvnRzQ8GI8OutXvAg8',
  authDomain: 'push-notification-manage-2116b.firebaseapp.com',
  projectId: 'push-notification-manage-2116b',
  storageBucket: 'push-notification-manage-2116b.firebasestorage.app',
  messagingSenderId: '945723382734',
  appId: '1:945723382734:web:32b83e0c6aec7dc67a299f',
  measurementId: 'G-BVW42WJX32',
};

// Public VAPID key provided by the user
const VAPID_KEY = 'BLY_UGwu_ah4bRGpEWD_dYxX5AxiHMQVKPIAvrnzsiV_cdmj-MiA1BIKa8zO4dHxF0J0HMoiE-RSxv-YCFZEnm0';

let firebaseInitialized = false;
let appInstance: any = null;
let fcmListenerRegistered = false;

function ensureInitialized() {
  if (!firebaseInitialized) {
    appInstance = initializeApp(firebaseConfig);
    firebaseInitialized = true;
  }
  return appInstance;
}

// Registers the service worker (must be at root /firebase-messaging-sw.js) and returns the token
export async function requestFcmTokenFromBrowser(): Promise<string> {
  if (!('serviceWorker' in navigator)) {
    throw new Error('Service workers are not supported in this browser');
  }

  // register SW if not already registered
  const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');

  ensureInitialized();
  const messaging = getMessaging(appInstance);

  // ask permission via the Notifications API
  const permission = await Notification.requestPermission();
  if (permission !== 'granted') {
    throw new Error('Notification permission not granted');
  }

  const token = await getToken(messaging, { vapidKey: VAPID_KEY, serviceWorkerRegistration: registration });
  if (!token) throw new Error('FCM token not obtained');
  return token;
}

// Optional helper to listen to foreground messages — registers only once
export function onFcmMessage(listener: (payload: any) => void) {
  ensureInitialized();
  const messaging = getMessaging(appInstance);
  if (fcmListenerRegistered) {
    return;
  }
  fcmListenerRegistered = true;
  onMessage(messaging, payload => {
    // Always log the payload first so the 'Foreground message' appears in console/logs
    try {
      // keep developer-friendly debug log
      // eslint-disable-next-line no-console
      console.debug('Foreground message:', payload);
    } catch (e) {
      // ignore
    }

    // Show a toast with notification content when in foreground
    try {
      const notification = (payload && (payload.notification || payload?.data)) || null;
      if (notification && typeof window !== 'undefined') {
        const title = notification.title || notification['title'] || 'Notificação';
        const body = notification.body || notification['body'] || JSON.stringify(notification);
        // show a non-blocking toast
        toast.info(`${title}: ${body}`);

        // send ack to backend (non-blocking)
        const rawNotificationId = (payload as any)?.data?.notificationId;
        const notificationId = rawNotificationId ? Number(rawNotificationId) : null;
        const messageId = (payload && (payload.messageId || (payload as any).data?.messageId)) || null;
        const ack = { notificationId, messageId, token: payload?.from || null, receivedAt: new Date().toISOString() };
        console.error(ack);
        fetch('/api/v1/notifications/internal/fcm/ack', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(ack),
        }).catch(() => {
          // ignore ACK errors
          console.error(`error`);
        });
      }
    } catch (e) {
      // ignore any toast errors
    }

    listener(payload);
  });
}
