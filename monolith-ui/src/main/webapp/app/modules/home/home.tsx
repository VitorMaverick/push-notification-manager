import './home.scss';

import React from 'react';
import { Translate } from 'react-jhipster';
import { Link } from 'react-router-dom';
import { Alert, Col, Row } from 'reactstrap';

import { useAppSelector } from 'app/config/store';

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);

  return (
    <Row>
      <Col md="6" className="pad">
        <span className="hipster rounded" />
      </Col>
      <Col md="6">
        <h1 className="display-4">
          <Translate contentKey="home.title">Welcome, Java Hipster!</Translate>
        </h1>
        <p className="lead">
          <Translate contentKey="home.subtitle">This is your homepage</Translate>
        </p>
        {account?.login ? (
          <div>
            <Alert color="success">
              <Translate contentKey="home.logged.message" interpolate={{ username: account.login }}>
                You are logged in as user {account.login}.
              </Translate>
            </Alert>
          </div>
        ) : (
          <div>
            <Alert color="warning">
              <Translate contentKey="global.messages.info.authenticated.prefix">If you want to </Translate>

              <Link to="/login" className="alert-link">
                <Translate contentKey="global.messages.info.authenticated.link"> sign in</Translate>
              </Link>
              <Translate contentKey="global.messages.info.authenticated.suffix">
                , you can try the default accounts:
                <br />- Administrator (login=&quot;admin&quot; and password=&quot;admin&quot;)
                <br />- User (login=&quot;user&quot; and password=&quot;user&quot;).
              </Translate>
            </Alert>

            <Alert color="warning">
              <Translate contentKey="global.messages.info.register.noaccount">You do not have an account yet?</Translate>&nbsp;
              <Link to="/account/register" className="alert-link">
                <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
              </Link>
            </Alert>
          </div>
        )}
        <p>
          <Translate contentKey="home.question">How to use the system:</Translate>
        </p>

        <ul>
          <li>
            <Link to="/device">
              <Translate contentKey="home.link.devices">Device Management</Translate>
            </Link>
            {' - '}
            <Translate contentKey="home.features.device_registration">Device registration with FCM tokens</Translate>
          </li>
          <li>
            <Link to="/notification/fcm/send">
              <Translate contentKey="home.link.send">Send Notifications</Translate>
            </Link>
            {' - '}
            <Translate contentKey="home.features.notification_sending">Send personalized push notifications</Translate>
          </li>
          <li>
            <Link to="/notification/history">
              <Translate contentKey="home.link.history">Notification History</Translate>
            </Link>
            {' - '}
            <Translate contentKey="home.features.notification_history">Detailed notification history</Translate>
          </li>
          <li>
            <a href="/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">
              <Translate contentKey="home.link.api">REST API</Translate>
            </a>
            {' - '}
            <Translate contentKey="home.features.real_time">Real-time notification support</Translate>
          </li>
        </ul>

        <div className="mt-4">
          <h4>
            <Translate contentKey="home.features.title">Main Features:</Translate>
          </h4>
          <ul className="list-unstyled">
            <li>
              ✅ <Translate contentKey="home.features.device_registration">Device registration with FCM tokens</Translate>
            </li>
            <li>
              ✅ <Translate contentKey="home.features.notification_sending">Send personalized push notifications</Translate>
            </li>
            <li>
              ✅ <Translate contentKey="home.features.device_management">Complete device management</Translate>
            </li>
            <li>
              ✅ <Translate contentKey="home.features.notification_history">Detailed notification history</Translate>
            </li>
            <li>
              ✅ <Translate contentKey="home.features.real_time">Real-time notification support</Translate>
            </li>
          </ul>
        </div>

        <Alert color="info" className="mt-4">
          <Translate contentKey="home.like">To get valid FCM tokens, use the</Translate>{' '}
          <strong>
            <Translate contentKey="home.github">Obtain Token from Browser</Translate>
          </strong>{' '}
          button in the Device Management section.
        </Alert>
      </Col>
    </Row>
  );
};

export default Home;
