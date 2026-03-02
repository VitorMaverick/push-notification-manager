import axios from 'axios';

const API_BASE = '/api/fcm';

const sendFcm = async (payload: any) => {
  try {
    const response = await axios.post(`${API_BASE}/send`, payload);
    return response.data;
  } catch (err: any) {
    // Normalize error message for the UI
    const msg = err?.response?.data?.message || err?.message || 'Unknown error';
    throw new Error(msg);
  }
};

export default {
  sendFcm,
};
