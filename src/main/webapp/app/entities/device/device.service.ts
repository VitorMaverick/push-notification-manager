import axios from 'axios';

const BASE = '/api/v1/devices';

export const registerDevice = (data: { fcmToken: string; platform?: string; userAgent?: string }) => axios.post(BASE, data);

export const listDevices = (params: { page: number; size: number }) => axios.get(BASE, { params });
