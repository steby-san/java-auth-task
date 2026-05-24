import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Loader2, AlertCircle } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import api from '../services/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const setToken = useAuthStore((state) => state.setToken);
  const setUser = useAuthStore((state) => state.setUser);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await api.post('/auth/login', { email, password });
      const { accessToken } = response.data;
      
      setToken(accessToken);
      
      const profileResponse = await api.get('/users/profile');
      setUser(profileResponse.data);

      navigate('/dashboard');
    } catch (err: any) {
      console.error('Lỗi đăng nhập', err);
      setError(err.response?.data?.message || 'Tài khoản hoặc mật khẩu không chính xác.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-slate-900">Đăng nhập</h2>
        <p className="text-sm text-slate-500 mt-1">Vui lòng điền thông tin để tiếp tục.</p>
      </div>
      
      {error && (
        <div className="text-red-600 bg-red-50 p-3 rounded-lg mb-6 text-sm flex items-start gap-2 border border-red-100">
          <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleLogin} className="space-y-5">
        <div className="space-y-1.5">
          <label className="block text-sm font-medium text-slate-700">Địa chỉ Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="minimal-input"
            placeholder="email"
            required
          />
        </div>

        <div className="space-y-1.5">
          <div className="flex justify-between items-center">
            <label className="block text-sm font-medium text-slate-700">Mật khẩu</label>
            <a href="#" className="text-sm text-slate-500 hover:text-black transition-colors">Quên mật khẩu?</a>
          </div>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="minimal-input"
            placeholder="••••••••"
            required
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="minimal-btn mt-2"
        >
          {loading ? (
            <Loader2 className="animate-spin h-5 w-5" />
          ) : (
            'Đăng nhập'
          )}
        </button>
      </form>

      <div className="mt-8 relative">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-slate-200"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-3 bg-white text-slate-400">Hoặc tiếp tục với</span>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-2 gap-3">
        <button
          type="button"
          onClick={() => window.location.href = `${import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080'}/oauth2/authorization/google`}
          className="w-full flex items-center justify-center px-4 py-2.5 border border-slate-200 rounded-xl bg-white hover:bg-slate-50 text-sm font-medium text-slate-700 transition-colors"
        >
          <svg className="h-4 w-4 mr-2" viewBox="0 0 24 24">
            <path d="M12.0003 4.75C13.7703 4.75 15.3553 5.36 16.6053 6.549L20.0303 3.125C17.9503 1.19 15.2353 0 12.0003 0C7.31028 0 3.25528 2.69 1.28027 6.609L5.27028 9.704C6.21528 6.86 8.87028 4.75 12.0003 4.75Z" fill="#EA4335"/>
            <path d="M23.49 12.275C23.49 11.49 23.415 10.73 23.3 10H12V14.51H18.47C18.18 15.99 17.34 17.25 16.08 18.1L19.945 21.1C22.2 19.01 23.49 15.92 23.49 12.275Z" fill="#4285F4"/>
            <path d="M5.26498 14.2949C5.02498 13.5699 4.88501 12.7999 4.88501 11.9999C4.88501 11.1999 5.01998 10.4299 5.26498 9.7049L1.275 6.60986C0.46 8.22986 0 10.0599 0 11.9999C0 13.9399 0.46 15.7699 1.28 17.3899L5.26498 14.2949Z" fill="#FBBC05"/>
            <path d="M12.0004 24.0001C15.2404 24.0001 17.9654 22.935 19.9454 21.095L16.0804 18.095C15.0054 18.82 13.6204 19.245 12.0004 19.245C8.8704 19.245 6.21538 17.135 5.26538 14.29L1.27539 17.385C3.25539 21.31 7.3104 24.0001 12.0004 24.0001Z" fill="#34A853"/>
          </svg>
          Google
        </button>
        <button
          type="button"
          onClick={() => window.location.href = `${import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080'}/oauth2/authorization/facebook`}
          className="w-full flex items-center justify-center px-4 py-2.5 border border-slate-200 rounded-xl bg-white hover:bg-slate-50 text-sm font-medium text-slate-700 transition-colors"
        >
          <svg className="h-4 w-4 mr-2 text-[#1877F2]" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path fillRule="evenodd" d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z" clipRule="evenodd" />
          </svg>
          Facebook
        </button>
      </div>

      <p className="mt-8 text-center text-sm text-slate-500">
        Bạn chưa có tài khoản?{' '}
        <Link to="/register" className="font-medium text-black hover:underline">
          Đăng ký ngay
        </Link>
      </p>
    </div>
  );
}
