import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import { Button, Form, FormGroup, Label, Input } from 'reactstrap';
import { toast, ToastContainer } from 'react-toastify';
import NotificationService from './notification.service';
import { requestFcmTokenFromBrowser, onFcmMessage } from 'app/firebaseClient';
import 'react-toastify/dist/ReactToastify.css';

type FormValues = {
  token: string;
  title: string;
  body: string;
  dados?: string;
};

const FcmSend = () => {
  const {
    control,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<FormValues>({
    defaultValues: { token: '', title: '', body: '', dados: '' },
  });
  const [loading, setLoading] = React.useState(false);
  const [logs, setLogs] = React.useState<string[]>([]);

  const addLog = (m: string) => setLogs(l => [new Date().toISOString() + ' ' + m, ...l].slice(0, 20));

  React.useEffect(() => {
    try {
      onFcmMessage(payload => addLog('Foreground message: ' + JSON.stringify(payload)));
    } catch (e) {
      // ignore if messaging not available
    }
  }, []);

  const obtainToken = async () => {
    addLog('Obtaining FCM token from browser...');
    try {
      const token = await requestFcmTokenFromBrowser();
      setValue('token', token);
      addLog('Obtained FCM token: ' + token);
      toast.success('Token obtained and filled in Token field');
    } catch (e) {
      console.error(e);
      addLog('Error obtaining token: ' + ((e as Error).message || JSON.stringify(e)));
      toast.error('Error obtaining token: ' + (e as Error).message);
    }
  };

  const onSubmit = async (data: FormValues) => {
    console.error('🔍 Form data received:', data);
    console.error('🔍 Title value:', data.title);
    console.error('🔍 Body value:', data.body);
    console.error('🔍 Token value:', data.token);

    addLog('onSubmit called with ' + JSON.stringify(data));
    setLoading(true);
    try {
      let parsedDados: any = undefined;
      if (data.dados && data.dados.trim() !== '') {
        try {
          parsedDados = JSON.parse(data.dados);
        } catch (pe) {
          toast.error('Data field contains invalid JSON: ' + (pe as Error).message);
          return;
        }
      }

      // Backend expects deviceToken/title/body field names
      const payload = {
        deviceToken: data.token,
        title: data.title,
        body: data.body,
        dados: parsedDados,
      };
      console.error('📤 Payload being sent:', payload);
      addLog('Sending payload ' + JSON.stringify(payload));
      const resp = await NotificationService.sendFcm(payload);
      addLog('Send response: ' + JSON.stringify(resp));
      toast.success('Notification sent successfully');
      reset();
    } catch (e) {
      console.error('Error sending FCM', e);
      addLog('Error sending FCM: ' + ((e as Error).message || JSON.stringify(e)));
      toast.error('Error sending: ' + (e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-3">
      <h2>Send Push Notification via FCM</h2>
      <ToastContainer />
      <div className="mb-2">
        <Button color="secondary" onClick={obtainToken} outline>
          Obtain FCM Token from Browser
        </Button>
      </div>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <FormGroup>
          <Label for="token">Token</Label>
          <Controller
            name="token"
            control={control}
            rules={{ required: 'Token is required' }}
            render={({ field }) => <Input id="token" {...field} placeholder="Device token" />}
          />
          {errors.token && <span className="text-danger">{errors.token.message}</span>}
        </FormGroup>
        <FormGroup>
          <Label for="title">Title</Label>
          <Controller
            name="title"
            control={control}
            rules={{ required: 'Title is required' }}
            render={({ field }) => <Input id="title" {...field} placeholder="Notification title" />}
          />
          {errors.title && <span className="text-danger">{errors.title.message}</span>}
        </FormGroup>
        <FormGroup>
          <Label for="body">Body</Label>
          <Controller
            name="body"
            control={control}
            rules={{ required: 'Body is required' }}
            render={({ field }) => <Input id="body" {...field} placeholder="Notification body" />}
          />
          {errors.body && <span className="text-danger">{errors.body.message}</span>}
        </FormGroup>
        <FormGroup>
          <Label for="dados">Data (JSON)</Label>
          <Controller
            name="dados"
            control={control}
            render={({ field }) => <Input id="dados" type="textarea" {...field} placeholder='{"key":"value"}' />}
          />
        </FormGroup>
        <Button type="submit" color="primary" disabled={loading} aria-busy={loading}>
          {loading ? 'Sending...' : 'Send Notification'}
        </Button>
      </Form>
      <div className="mt-3">
        <h5>Debug logs</h5>
        <div style={{ maxHeight: 200, overflow: 'auto', background: '#f8f9fa', padding: 8 }}>
          {logs.map((l, i) => (
            <div key={i} style={{ fontFamily: 'monospace', fontSize: 12 }}>
              {l}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default FcmSend;
