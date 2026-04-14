import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Button, Form, FormGroup, Label, Input, Table } from 'reactstrap';
import { toast, ToastContainer } from 'react-toastify';
import { registerDevice, listDevices } from './device.service';
import 'react-toastify/dist/ReactToastify.css';

interface RegisterForm {
  fcmToken: string;
  platform?: string;
  userAgent?: string;
}

interface DeviceRow {
  id: number;
  fcmToken: string;
  platform?: string;
  userAgent?: string;
  registeredAt?: string;
  lastUsedAt?: string;
}

const PAGE_SIZES = [10, 20, 50];

const DeviceManagement = () => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RegisterForm>();
  const [registering, setRegistering] = useState(false);

  const [devices, setDevices] = useState<DeviceRow[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [loadingList, setLoadingList] = useState(false);

  const fetchDevices = async () => {
    setLoadingList(true);
    try {
      const res = await listDevices({ page, size });
      setDevices(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
    } catch (e: any) {
      toast.error('Error loading devices: ' + (e?.message || 'Unknown error'));
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    fetchDevices();
  }, [page, size]);

  const onSubmit = async (data: RegisterForm) => {
    setRegistering(true);
    try {
      await registerDevice({ fcmToken: data.fcmToken, platform: data.platform || undefined, userAgent: data.userAgent || undefined });
      toast.success('Device registered successfully');
      reset();
      setPage(0);
      fetchDevices();
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 400) {
        toast.error('Invalid FCM token');
      } else if (status === 409) {
        toast.error('Token already registered');
      } else {
        toast.error('Error registering device: ' + (e?.message || 'Unknown error'));
      }
    } finally {
      setRegistering(false);
    }
  };

  const copyToken = (token: string) => {
    navigator.clipboard
      .writeText(token)
      .then(() => toast.success('Token copied to clipboard'))
      .catch(() => toast.error('Failed to copy token'));
  };

  return (
    <div className="container mt-3">
      <h2>Device Management</h2>
      <ToastContainer />

      <div className="card mb-4">
        <div className="card-body">
          <h5 className="card-title">Register Device</h5>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <FormGroup>
              <Label for="fcmToken">FCM Token</Label>
              <Input id="fcmToken" {...register('fcmToken', { required: 'FCM token is required' })} placeholder="FCM device token" />
              {errors.fcmToken && <span className="text-danger">{errors.fcmToken.message}</span>}
            </FormGroup>
            <FormGroup>
              <Label for="platform">Platform</Label>
              <Input type="select" id="platform" {...register('platform')}>
                <option value="">Select platform (optional)</option>
                <option value="ANDROID">Android</option>
                <option value="IOS">iOS</option>
                <option value="WEB">Web</option>
              </Input>
            </FormGroup>
            <FormGroup>
              <Label for="userAgent">User Agent</Label>
              <Input id="userAgent" {...register('userAgent')} placeholder="User agent / device name (optional)" />
            </FormGroup>
            <Button type="submit" color="primary" disabled={registering}>
              {registering ? 'Registering...' : 'Register Device'}
            </Button>
          </Form>
        </div>
      </div>

      <div className="d-flex justify-content-between align-items-center mb-2">
        <h5>Registered Devices</h5>
        <div className="d-flex gap-2 align-items-center">
          <Input
            type="select"
            style={{ width: 80 }}
            value={size}
            onChange={e => {
              setSize(Number(e.target.value));
              setPage(0);
            }}
          >
            {PAGE_SIZES.map(s => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </Input>
          <Button size="sm" color="secondary" onClick={fetchDevices} disabled={loadingList}>
            {loadingList ? 'Loading...' : 'Refresh'}
          </Button>
        </div>
      </div>

      <Table bordered responsive hover size="sm">
        <thead>
          <tr>
            <th>ID</th>
            <th>Token</th>
            <th>Platform</th>
            <th>User Agent</th>
            <th>Registered At</th>
            <th>Last Used At</th>
          </tr>
        </thead>
        <tbody>
          {devices.length === 0 ? (
            <tr>
              <td colSpan={6} className="text-center">
                No devices registered.
              </td>
            </tr>
          ) : (
            devices.map(d => (
              <tr key={d.id}>
                <td>{d.id}</td>
                <td style={{ fontFamily: 'monospace', fontSize: 11 }}>
                  {d.fcmToken.substring(0, 8)}…
                  <Button size="sm" color="link" className="p-0 ms-1" onClick={() => copyToken(d.fcmToken)} title="Copy token">
                    📋
                  </Button>
                </td>
                <td>{d.platform ?? '—'}</td>
                <td>{d.userAgent ?? '—'}</td>
                <td>{d.registeredAt ? new Date(d.registeredAt).toLocaleString() : '—'}</td>
                <td>{d.lastUsedAt ? new Date(d.lastUsedAt).toLocaleString() : '—'}</td>
              </tr>
            ))
          )}
        </tbody>
      </Table>

      <div className="d-flex gap-2 align-items-center">
        <Button size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>
          Previous
        </Button>
        <span>
          Page {page + 1} of {totalPages || 1}
        </span>
        <Button size="sm" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>
          Next
        </Button>
      </div>
    </div>
  );
};

export default DeviceManagement;
