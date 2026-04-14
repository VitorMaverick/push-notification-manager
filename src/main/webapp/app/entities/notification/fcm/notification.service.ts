import axios from 'axios';

const FCM_BASE = '/api/fcm';
const V1_BASE = '/api/v1/notifications';

const sendFcm = async (payload: any) => {
  try {
    const response = await axios.post(`${FCM_BASE}/send`, payload);
    return response.data;
  } catch (err: any) {
    const msg = err?.response?.data?.message || err?.message || 'Unknown error';
    throw new Error(msg);
  }
};

const getHistory = (params: Record<string, any>) => axios.get(V1_BASE, { params });

const getById = (id: number) => axios.get(`${V1_BASE}/${id}`);

export default {
  sendFcm,
  getHistory,
  getById,
};
