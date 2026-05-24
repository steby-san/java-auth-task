import { Outlet } from 'react-router-dom';

export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-slate-50 font-outfit flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-[420px] animate-fade-in-up">
        
        <div className="minimal-card p-8 sm:p-10">
          <Outlet />
        </div>
        
        <div className="mt-8 text-center text-xs text-slate-400">
          <p>&copy; 2026</p>
        </div>
      </div>
    </div>
  );
}
