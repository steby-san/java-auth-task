import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut, User, Shield, Activity, Fingerprint, Clock, ShieldCheck, Mail } from 'lucide-react';
import { useAuthStore } from '../store/useAuthStore';
import api from '../services/api';

export default function Dashboard() {
  const { user, logout, isAuthenticated } = useAuthStore();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    const fetchProfile = async () => {
      try {
        const response = await api.get('/users/profile');
        setProfile(response.data);
      } catch (error) {
        console.error('Không thể lấy thông tin profile', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [isAuthenticated, navigate]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 font-outfit flex items-center justify-center">
        <div className="h-12 w-12 bg-black text-white rounded-2xl flex items-center justify-center animate-pulse">
          <ShieldCheck className="h-6 w-6" />
        </div>
      </div>
    );
  }

  const displayUser = profile || user;

  return (
    <div className="min-h-screen bg-slate-50 font-outfit text-slate-900">
      <nav className="bg-white border-b border-slate-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <div className="h-8 w-8 bg-black text-white rounded-lg flex items-center justify-center">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
              </div>
            </div>
            <div>
              <button
                onClick={handleLogout}
                className="flex items-center px-4 py-2 text-sm font-medium text-slate-600 hover:text-black transition-colors rounded-lg hover:bg-slate-100"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-2xl font-semibold mb-1">Xin chào, {displayUser?.firstName}!</h1>
          <p className="text-slate-500 text-sm">Quản lý tài khoản và phiên đăng nhập của bạn.</p>
        </div>

        <div className="flex justify-center">
          <div className="minimal-card p-6 sm:p-8 w-1/2">
            <div className="flex items-center mb-6 pb-6 border-b border-slate-100">
              <div className="h-12 w-12 rounded-full bg-slate-100 flex items-center justify-center mr-4">
                <User className="h-6 w-6 text-slate-600" />
              </div>
              <div>
                <h2 className="text-lg font-semibold text-slate-900">Hồ sơ cá nhân</h2>
                <p className="text-sm text-slate-500">Thông tin định danh</p>
              </div>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="p-4 rounded-xl border border-slate-100 bg-slate-50/50">
                <div className="flex items-center gap-2 mb-1.5">
                  <Fingerprint className="w-4 h-4 text-slate-400" />
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">ID Tài khoản</span>
                </div>
                <div className="font-mono text-slate-900 font-medium">{displayUser?.id || 'N/A'}</div>
              </div>

              <div className="p-4 rounded-xl border border-slate-100 bg-slate-50/50">
                <div className="flex items-center gap-2 mb-1.5">
                  <User className="w-4 h-4 text-slate-400" />
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Họ và tên</span>
                </div>
                <div className="text-slate-900 font-medium">
                  {displayUser?.firstName} {displayUser?.lastName}
                </div>
              </div>

              <div className="p-4 rounded-xl border border-slate-100 bg-slate-50/50 md:col-span-2">
                <div className="flex items-center gap-2 mb-1.5">
                  <Mail className="w-4 h-4 text-slate-400" />
                  <span className="text-xs font-medium text-slate-500 uppercase tracking-wider">Email</span>
                </div>
                <div className="text-slate-900 font-medium">{displayUser?.email}</div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
