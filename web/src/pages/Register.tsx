import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Loader2, AlertCircle } from 'lucide-react';
import api from '../services/api';

export default function Register() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await api.post('/auth/register', {
        firstName,
        lastName,
        email,
        password,
      });
      navigate('/login');
    } catch (err: any) {
      console.error('Lỗi đăng ký', err);
      setError(err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại sau.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full">
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-slate-900">Tạo tài khoản mới</h2>
        <p className="text-sm text-slate-500 mt-1">Vui lòng điền thông tin để tiếp tục.</p>
      </div>
      
      {error && (
        <div className="text-red-600 bg-red-50 p-3 rounded-lg mb-6 text-sm flex items-start gap-2 border border-red-100">
          <AlertCircle className="h-5 w-5 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleRegister} className="space-y-5">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-slate-700">Tên</label>
            <input
              type="text"
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              className="minimal-input"
              placeholder="Văn A"
              required
            />
          </div>
          <div className="space-y-1.5">
            <label className="block text-sm font-medium text-slate-700">Họ</label>
            <input
              type="text"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              className="minimal-input"
              placeholder="Nguyễn"
              required
            />
          </div>
        </div>

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
          <label className="block text-sm font-medium text-slate-700">Mật khẩu</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="minimal-input"
            placeholder="••••••••"
            required
            minLength={6}
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
            'Đăng ký ngay'
          )}
        </button>
      </form>

      <p className="mt-8 text-center text-sm text-slate-500">
        Bạn đã có tài khoản?{' '}
        <Link to="/login" className="font-medium text-black hover:underline">
          Đăng nhập
        </Link>
      </p>
    </div>
  );
}
