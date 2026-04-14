import axios from 'axios';

const BASE = '/api/v1/notifications';

export const getHistory = (params: Record<string, any>) => axios.get(BASE, { params });
export const getById = (id: number) => axios.get(`${BASE}/${id}`);
