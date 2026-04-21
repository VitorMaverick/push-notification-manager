import axios from 'axios';

const V1_BASE = '/api/v1/notifications';

const sendFcm = async (payload: any) => {
  try {
    // Use the new v1 notifications endpoint for creating/sending notifications
    const response = await axios.post(`${V1_BASE}`, payload);
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
