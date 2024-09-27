import { jwtDecode } from 'jwt-decode';
import type { StateCreator } from 'zustand';

const INITIAL_STATE: Omit<AuthSlice, 'login' | 'logout'> = {
  isLoggedIn: false,
  accessToken: '',
  refreshToken: '',
  role: null,
  crewStatus: null,
};

const createAuthSlice: StateCreator<RootState, [], [], AuthSlice> = (set) => ({
  ...INITIAL_STATE,
  login: (authData: LoginResponseData) => {
    const { crewStatus, role } = jwtDecode<AccessTokenPayload>(authData.accessToken);
    set({
      ...authData,
      isLoggedIn: true,
      role,
      crewStatus,
    });
  },
  logout: () => set({ ...INITIAL_STATE }),
});

export default createAuthSlice;
