/* eslint-disable no-undef */
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.22.1/firebase-messaging-compat.js');

// Use the same firebase config as the client. This file runs as a Service Worker so keep it minimal.
const firebaseConfig = {
  apiKey: 'AIzaSyAi5BxuPTP8OPtCtJvnRzQ8GI8OutXvAg8',
  authDomain: 'push-notification-manage-2116b.firebaseapp.com',
  projectId: 'push-notification-manage-2116b',
  storageBucket: 'push-notification-manage-2116b.firebasestorage.app',
  messagingSenderId: '945723382734',
  appId: '1:945723382734:web:32b83e0c6aec7dc67a299f',
  measurementId: 'G-BVW42WJX32',
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function (payload) {
  console.log('[firebase-messaging-sw.js] Received background message ', payload);
  const notification = payload.notification || {};
  self.registration.showNotification(notification.title || 'Background Message', {
    body: notification.body,
    icon: notification.icon,
  });
});
