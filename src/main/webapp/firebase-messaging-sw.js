/* eslint-disable no-undef */
/* Firebase Messaging Service Worker
 * Uses Firebase compat SDK v9 (importScripts from gstatic CDN).
 * The CSP in application.yml must allow https://www.gstatic.com in script-src
 * for these importScripts calls to succeed.
 */
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-messaging-compat.js');

// Firebase project configuration — must match firebaseClient.ts
const firebaseConfig = {
  apiKey: 'AIzaSyAi5BxuPTP8OPtCtJvnRzQ8GI8OutXvAg8',
  authDomain: 'push-notification-manage-2116b.firebaseapp.com',
  projectId: 'push-notification-manage-2116b',
  storageBucket: 'push-notification-manage-2116b.firebasestorage.app',
  messagingSenderId: '945723382734',
  appId: '1:945723382734:web:32b83e0c6aec7dc67a299f',
  measurementId: 'G-BVW42WJX32',
};

// Guard against double-initialization (hot reload scenarios)
if (!firebase.apps.length) {
  firebase.initializeApp(firebaseConfig);
}

const messaging = firebase.messaging();

/**
 * Handle background messages (when the app is not in the foreground).
 * The browser shows a native notification automatically if the payload has
 * a 'notification' field, so we only need to handle 'data-only' messages here.
 */
messaging.onBackgroundMessage(function (payload) {
  // eslint-disable-next-line no-console
  console.log('[firebase-messaging-sw.js] Received background message:', payload);

  // If the payload already contains a notification, the browser renders it automatically.
  // We only build a custom notification for data-only messages.
  const notification = payload.notification || payload.data || {};
  const title = notification.title || 'New Notification';
  const body = notification.body || '';
  const icon = notification.icon || '/favicon.ico';

  return self.registration.showNotification(title, {
    body: body,
    icon: icon,
    badge: '/favicon.ico',
    data: payload.data || {},
  });
});

// Keep the service worker alive during long async operations
self.addEventListener('push', function (event) {
  // Firebase SDK handles push events internally; this listener is a safety net
  // to prevent the SW from being terminated before Firebase processes the event.
  if (!event.data) return;
});

// Handle notification click — open or focus the app window
self.addEventListener('notificationclick', function (event) {
  event.notification.close();

  const urlToOpen = event.notification.data && event.notification.data.link ? event.notification.data.link : self.location.origin;

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then(function (clientList) {
      for (const client of clientList) {
        if (client.url.startsWith(self.location.origin) && 'focus' in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(urlToOpen);
      }
    }),
  );
});
