import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import { Button, Form, FormGroup, Label, Input } from 'reactstrap';
import NotificationService from './notification.service';
import axios from 'axios';

type FormValues = {
  token: string;
  titulo?: string;
  corpo?: string;
  dados?: string; // JSON string
};

const FcmSend = () => {
  const { control, handleSubmit, reset } = useForm<FormValues>({ defaultValues: { token: '', titulo: '', corpo: '', dados: '' } });
  const [loading, setLoading] = React.useState(false);
  const [logs, setLogs] = React.useState<string[]>([]);

  const addLog = (m: string) => setLogs(l => [new Date().toISOString() + ' ' + m, ...l].slice(0, 20));

  const onSubmit = async (data: FormValues) => {
    addLog('onSubmit called with ' + JSON.stringify(data));
    setLoading(true);
    try {
      let parsedDados: any = undefined;
      if (data.dados && data.dados.trim() !== '') {
        try {
          parsedDados = JSON.parse(data.dados);
        } catch (pe) {
          alert('Campo Dados contém JSON inválido: ' + (pe as Error).message);
          return;
        }
      }

      const payload = {
        token: data.token,
        titulo: data.titulo,
        corpo: data.corpo,
        dados: parsedDados,
      };
      addLog('Sending payload ' + JSON.stringify(payload));
      const resp = await NotificationService.sendFcm(payload);
      addLog('Send response: ' + JSON.stringify(resp));
      alert('Enviado para fila (ou disparado) com sucesso');
      reset();
    } catch (e) {
      console.error('Error sending FCM', e);
      addLog('Error sending FCM: ' + ((e as Error).message || JSON.stringify(e)));
      alert('Erro ao enviar: ' + (e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-3">
      <h2>Enviar Push via FCM</h2>
      <Form onSubmit={handleSubmit(onSubmit)}>
        <FormGroup>
          <Label for="token">Token</Label>
          <Controller
            name="token"
            control={control}
            rules={{ required: true }}
            render={({ field }) => <Input id="token" {...field} placeholder="token do dispositivo" />}
          />
        </FormGroup>
        <FormGroup>
          <Label for="titulo">Título</Label>
          <Controller
            name="titulo"
            control={control}
            render={({ field }) => <Input id="titulo" {...field} placeholder="Título da notificação" />}
          />
        </FormGroup>
        <FormGroup>
          <Label for="corpo">Corpo</Label>
          <Controller
            name="corpo"
            control={control}
            render={({ field }) => <Input id="corpo" {...field} placeholder="Texto da notificação" />}
          />
        </FormGroup>
        <FormGroup>
          <Label for="dados">Dados (JSON)</Label>
          <Controller
            name="dados"
            control={control}
            render={({ field }) => <Input id="dados" type="textarea" {...field} placeholder='{"key":"value"}' />}
          />
        </FormGroup>
        <Button type="submit" color="primary" disabled={loading} aria-busy={loading}>
          {loading ? 'Enviando...' : 'Enviar'}
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
