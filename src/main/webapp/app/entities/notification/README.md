# Notification UI

This folder contains a simple React form to send push notifications via the backend `/api/fcm/send` endpoint.

How to use

1. Start backend and frontend (development):

```bash
# In project root
./mvnw
# In another terminal
npm start
```

2. Open the app and navigate to `/notification/fcm/send` (or add a menu link). Fill the device token and message fields and press Enviar.

Notes

- The form sends a JSON body with `token`, `titulo`, `corpo` and optional `dados` (JSON object).
- Authentication: the backend endpoint expects an authenticated user; in development you can login via the app and then use the form.
- The frontend uses `axios` already included in the project.
