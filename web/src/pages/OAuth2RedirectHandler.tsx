import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import api from '../services/api';

const OAuth2RedirectHandler = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { setToken, setUser } = useAuthStore();

    useEffect(() => {
        const token = searchParams.get('token');
        
        if (token) {
            setToken(token);
            
            api.get('/users/profile')
                .then((res) => {
                    setUser(res.data);
                    navigate('/dashboard', { replace: true });
                })
                .catch((err) => {
                    console.error('Failed to fetch user profile after OAuth2 login', err);
                    navigate('/login', { replace: true, state: { error: 'Đăng nhập thất bại. Vui lòng thử lại.' } });
                });
        } else {
            navigate('/login', { replace: true, state: { error: 'Không tìm thấy token xác thực.' } });
        }
    }, [searchParams, navigate, setToken, setUser]);

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center font-outfit">
            <div className="flex flex-col items-center gap-4">
                <div className="h-10 w-10 border-4 border-slate-200 border-t-black rounded-full animate-spin"></div>
                <p className="text-slate-600 font-medium">Đang xử lý đăng nhập...</p>
            </div>
        </div>
    );
};

export default OAuth2RedirectHandler;
