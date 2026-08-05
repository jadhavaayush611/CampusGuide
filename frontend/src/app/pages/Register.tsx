import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router';
import { Mail, Lock, User, Eye, EyeOff, Loader2, UserPlus, AlertCircle, Building, IdCard } from 'lucide-react';
import { useRegister } from '../../hooks/auth/useRegister';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Alert, AlertDescription } from '../components/ui/alert';

export function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [role, setRole] = useState('STUDENT');
  const [department, setDepartment] = useState('');
  const [studentId, setStudentId] = useState('');

  const [showPassword, setShowPassword] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<{
    name?: string;
    email?: string;
    password?: string;
    confirmPassword?: string;
  }>({});

  const registerMutation = useRegister();
  const navigate = useNavigate();
  const location = useLocation();

  const from = (location.state as { from?: Location })?.from?.pathname || '/';

  const validate = (): boolean => {
    const errors: {
      name?: string;
      email?: string;
      password?: string;
      confirmPassword?: string;
    } = {};

    if (!name.trim()) {
      errors.name = 'Full name is required';
    }

    if (!email.trim()) {
      errors.email = 'Email address is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      errors.email = 'Please enter a valid email address';
    }

    if (!password) {
      errors.password = 'Password is required';
    } else if (password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
    }

    if (password !== confirmPassword) {
      errors.confirmPassword = 'Passwords do not match';
    }

    setFieldErrors(errors);

    // Focus the first invalid field
    if (errors.name) {
      document.getElementById('name')?.focus();
    } else if (errors.email) {
      document.getElementById('email')?.focus();
    } else if (errors.password) {
      document.getElementById('password')?.focus();
    } else if (errors.confirmPassword) {
      document.getElementById('confirmPassword')?.focus();
    }

    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    registerMutation.mutate(
      {
        name: name.trim(),
        email: email.trim(),
        password,
        role,
        department: department.trim() || undefined,
        studentId: studentId.trim() || undefined,
      },
      {
        onSuccess: () => {
          navigate(from, { replace: true });
        },
      }
    );
  };

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="w-full max-w-lg space-y-6 bg-white p-8 rounded-2xl shadow-xl border border-gray-100">
        <div className="text-center space-y-2">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-purple-50 text-[#7C3AED] mb-1">
            <UserPlus className="w-6 h-6" />
          </div>
          <h2 className="text-2xl font-bold text-gray-900 tracking-tight">Create your account</h2>
          <p className="text-sm text-gray-500">Join CampusGuide to access all campus resources</p>
        </div>

        {registerMutation.isError && (
          <Alert variant="destructive" role="alert" className="bg-red-50 text-red-700 border border-red-200 rounded-xl">
            <AlertCircle className="h-4 w-4 text-red-600" />
            <AlertDescription className="text-sm">
              {registerMutation.error?.message || 'Failed to create account. Please check your information.'}
            </AlertDescription>
          </Alert>
        )}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div className="space-y-1.5">
            <Label htmlFor="name" className="text-sm font-medium text-gray-700">
              Full Name *
            </Label>
            <div className="relative">
              <User className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <Input
                id="name"
                type="text"
                placeholder="Alex Johnson"
                value={name}
                onChange={(e) => {
                  setName(e.target.value);
                  if (fieldErrors.name) setFieldErrors((prev) => ({ ...prev, name: undefined }));
                }}
                disabled={registerMutation.isPending}
                className="pl-10 h-11 rounded-xl bg-gray-50 border-gray-200 focus:bg-white text-gray-900"
                aria-invalid={Boolean(fieldErrors.name)}
                aria-describedby={fieldErrors.name ? "name-error" : undefined}
                autoComplete="name"
              />
            </div>
            {fieldErrors.name && <p id="name-error" className="text-xs text-red-600 mt-1">{fieldErrors.name}</p>}
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="email" className="text-sm font-medium text-gray-700">
              Email Address *
            </Label>
            <div className="relative">
              <Mail className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <Input
                id="email"
                type="email"
                placeholder="alex@campusguide.edu"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (fieldErrors.email) setFieldErrors((prev) => ({ ...prev, email: undefined }));
                }}
                disabled={registerMutation.isPending}
                className="pl-10 h-11 rounded-xl bg-gray-50 border-gray-200 focus:bg-white text-gray-900"
                aria-invalid={Boolean(fieldErrors.email)}
                aria-describedby={fieldErrors.email ? "email-error" : undefined}
                autoComplete="email"
              />
            </div>
            {fieldErrors.email && <p id="email-error" className="text-xs text-red-600 mt-1">{fieldErrors.email}</p>}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label htmlFor="role" className="text-sm font-medium text-gray-700">
                Role
              </Label>
              <select
                id="role"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                disabled={registerMutation.isPending}
                className="w-full h-11 px-3 rounded-xl bg-gray-50 border border-gray-200 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[#2563EB]/50"
              >
                <option value="STUDENT">Student</option>
                <option value="FACULTY">Faculty</option>
                <option value="STAFF">Staff</option>
              </select>
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="studentId" className="text-sm font-medium text-gray-700">
                Student / ID No.
              </Label>
              <div className="relative">
                <IdCard className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <Input
                  id="studentId"
                  type="text"
                  placeholder="CS-2026-042"
                  value={studentId}
                  onChange={(e) => setStudentId(e.target.value)}
                  disabled={registerMutation.isPending}
                  className="pl-10 h-11 rounded-xl bg-gray-50 border-gray-200 focus:bg-white text-gray-900"
                />
              </div>
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="department" className="text-sm font-medium text-gray-700">
              Department / Faculty
            </Label>
            <div className="relative">
              <Building className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <Input
                id="department"
                type="text"
                placeholder="Computer Science & Engineering"
                value={department}
                onChange={(e) => setDepartment(e.target.value)}
                disabled={registerMutation.isPending}
                className="pl-10 h-11 rounded-xl bg-gray-50 border-gray-200 focus:bg-white text-gray-900"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <Label htmlFor="password" className="text-sm font-medium text-gray-700">
                Password *
              </Label>
              <div className="relative">
                <Lock className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (fieldErrors.password) setFieldErrors((prev) => ({ ...prev, password: undefined }));
                  }}
                  disabled={registerMutation.isPending}
                  className="pl-10 pr-10 h-11 rounded-xl bg-gray-50 border-gray-200 focus:bg-white text-gray-900"
                  aria-invalid={Boolean(fieldErrors.password)}
                  aria-describedby={fieldErrors.password ? "password-error" : undefined}
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 rounded-md"
                  aria-label={showPassword ? "Hide password" : "Show password"}
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {fieldErrors.password && <p id="password-error" className="text-xs text-red-600 mt-1">{fieldErrors.password}</p>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="confirmPassword" className="text-sm font-medium text-gray-700">
                Confirm Password *
              </Label>
              <div className="relative">
                <Lock className="w-5 h-5 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <Input
                  id="confirmPassword"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => {
                    setConfirmPassword(e.target.value);
                    if (fieldErrors.confirmPassword)
                      setFieldErrors((prev) => ({ ...prev, confirmPassword: undefined }));
                  }}
                  disabled={registerMutation.isPending}
                  className="pl-10 h-11 rounded-xl bg-gray-50 border-gray-200 focus:bg-white text-gray-900"
                  aria-invalid={Boolean(fieldErrors.confirmPassword)}
                  aria-describedby={fieldErrors.confirmPassword ? "confirmPassword-error" : undefined}
                  autoComplete="new-password"
                />
              </div>
              {fieldErrors.confirmPassword && (
                <p id="confirmPassword-error" className="text-xs text-red-600 mt-1">{fieldErrors.confirmPassword}</p>
              )}
            </div>
          </div>

          <Button
            type="submit"
            disabled={registerMutation.isPending}
            aria-busy={registerMutation.isPending}
            className="w-full h-11 bg-[#7C3AED] hover:bg-purple-700 text-white font-medium rounded-xl transition-colors shadow-sm disabled:opacity-50 mt-2"
          >
            {registerMutation.isPending ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin mr-2" />
                Creating account...
              </>
            ) : (
              'Create Account'
            )}
          </Button>
        </form>

        <div className="pt-4 border-t border-gray-100 text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-[#7C3AED] hover:underline">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
