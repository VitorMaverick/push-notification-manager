import React, { useState, useEffect } from 'react';
import { Button, Table, Input, Badge } from 'reactstrap';
import { toast } from 'react-toastify';
import { getHistory } from './notificationHistory.service';

type NotificationStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'FAILED';

interface NotificationSummary {
  id: number;
  title?: string;
  status: NotificationStatus;
  recipientToken?: string;
  createdAt?: string;
  sentAt?: string;
  deliveredAt?: string;
}

const STATUS_BADGE: Record<NotificationStatus, string> = {
  PENDING: 'secondary',
  SENT: 'primary',
  DELIVERED: 'success',
  FAILED: 'danger',
};

const PAGE_SIZES = [10, 20, 50];

const NotificationHistory = () => {
  const [notifications, setNotifications] = useState<NotificationSummary[]>([]);
  const [status, setStatus] = useState('');
  const [deviceToken, setDeviceToken] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const params: Record<string, any> = { page, size };
      if (status) params.status = status;
      if (deviceToken) params.deviceToken = deviceToken;
      if (fromDate) params.fromDate = new Date(fromDate).toISOString();
      if (toDate) params.toDate = new Date(toDate).toISOString();
      const res = await getHistory(params);
      setNotifications(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
    } catch (e: any) {
      toast.error('Error loading notifications: ' + (e?.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [page, size]);

  return (
    <div className="container mt-3">
      <h2>Notification History</h2>

      <div className="row mb-3 g-2">
        <div className="col-md-2">
          <Input type="select" value={status} onChange={e => setStatus(e.target.value)}>
            <option value="">All Statuses</option>
            <option value="PENDING">PENDING</option>
            <option value="SENT">SENT</option>
            <option value="DELIVERED">DELIVERED</option>
            <option value="FAILED">FAILED</option>
          </Input>
        </div>
        <div className="col-md-3">
          <Input type="text" placeholder="Device token filter" value={deviceToken} onChange={e => setDeviceToken(e.target.value)} />
        </div>
        <div className="col-md-2">
          <Input type="date" value={fromDate} onChange={e => setFromDate(e.target.value)} />
        </div>
        <div className="col-md-2">
          <Input type="date" value={toDate} onChange={e => setToDate(e.target.value)} />
        </div>
        <div className="col-md-1">
          <Input
            type="select"
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
        </div>
        <div className="col-md-2">
          <Button
            color="primary"
            onClick={() => {
              setPage(0);
              fetchData();
            }}
            disabled={loading}
          >
            {loading ? 'Loading...' : 'Refresh'}
          </Button>
        </div>
      </div>

      <Table bordered responsive hover size="sm">
        <thead>
          <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Status</th>
            <th>Token</th>
            <th>Created At</th>
            <th>Sent At</th>
            <th>Delivered At</th>
          </tr>
        </thead>
        <tbody>
          {notifications.length === 0 ? (
            <tr>
              <td colSpan={7} className="text-center">
                No notifications found.
              </td>
            </tr>
          ) : (
            notifications.map(n => (
              <tr key={n.id}>
                <td>{n.id}</td>
                <td>{n.title ?? '—'}</td>
                <td>
                  <Badge color={STATUS_BADGE[n.status] ?? 'secondary'}>{n.status}</Badge>
                </td>
                <td style={{ fontFamily: 'monospace', fontSize: 11 }}>
                  {n.recipientToken ? n.recipientToken.substring(0, 12) + '…' : '—'}
                </td>
                <td>{n.createdAt ? new Date(n.createdAt).toLocaleString() : '—'}</td>
                <td>{n.sentAt ? new Date(n.sentAt).toLocaleString() : '—'}</td>
                <td>{n.deliveredAt ? new Date(n.deliveredAt).toLocaleString() : '—'}</td>
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

export default NotificationHistory;
