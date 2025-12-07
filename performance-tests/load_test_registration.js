import http from 'k6/http';
import { check, sleep } from 'k6';
import exec from 'k6/execution';

export const options = {
    vus: 50,
    iterations: 5000,
    thresholds: {
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const globalIter = exec.scenario.iterationInTest;
    const i = globalIter + 1;

    const payload = JSON.stringify({
        username: `user${i}`,
        password: 'Password123!',
        email: `user${i}@example.com`
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(`${BASE_URL}/auth/register`, payload, params);


    check(res, {
        'registered successfully': (r) => r.status === 200 || r.status === 201,
    });

    if (res.status !== 200 && res.status !== 201 && res.status !== 409) {
        console.log(`Failed to register user${i}: ${res.status} ${res.body}`);
    }
}
