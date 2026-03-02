import React from 'react';

import { Route } from 'react-router'; // eslint-disable-line

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

/* jhipster-needle-add-route-import - JHipster will add routes here */
import FcmSend from './notification/fcm/FcmSend';

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
        <Route path="notification/fcm/send" element={<FcmSend />} />
      </ErrorBoundaryRoutes>
    </div>
  );
};
