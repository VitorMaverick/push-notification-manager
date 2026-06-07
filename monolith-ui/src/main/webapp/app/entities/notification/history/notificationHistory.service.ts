import axios from 'axios';

const BASE = `${NOTIFICATION_SERVICE_URL}/api/v1/notifications`;

export const getHistory = (params: Record<string, any>) => axios.get(BASE, { params });
export const getById = (id: number) => axios.get(`${BASE}/${id}`);
