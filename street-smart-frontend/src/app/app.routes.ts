import { Routes } from '@angular/router';
import { AuthGuard } from './auth.guard';
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./landing/landing.component').then((m) => m.LandingComponent),
  },
  {
    path: 'login',
    loadComponent: () => import('./auth/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'about',
    loadComponent: () => import('./about/about.component').then((m) => m.AboutComponent),
  },
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    data: { roles: ['USER'] },
    loadComponent: () =>
      import('./dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'shop-dashboard',
    canActivate: [AuthGuard],
    data: { roles: ['SHOPKEEPER'] },
    loadComponent: () =>
      import('./shop-dashboard/shop-dashboard.component').then((m) => m.ShopDashboardComponent),
  },
  {
    path: 'shop-registration',
    canActivate: [AuthGuard],
    data: { roles: ['SHOPKEEPER'] },
    loadComponent: () =>
      import('./shopkeeper-registration/shopkeeper-registration.component').then(
        (m) => m.ShopkeeperRegistrationComponent,
      ),
  },
  { path: 'shop-rejected', redirectTo: 'shop-dashboard', pathMatch: 'full' },
  {
    path: 'products',
    canActivate: [AuthGuard],
    data: { roles: ['SHOPKEEPER'] },
    loadComponent: () => import('./products/products.component').then((m) => m.ProductsComponent),
  },
  {
    path: 'reviews',
    canActivate: [AuthGuard],
    data: { roles: ['SHOPKEEPER'] },
    loadComponent: () => import('./reviews/reviews.component').then((m) => m.ReviewsComponent),
  },
  {
    path: 'favorites',
    canActivate: [AuthGuard],
    data: { roles: ['USER'] },
    loadComponent: () =>
      import('./favorites/favorites.component').then((m) => m.FavoritesComponent),
  },
  {
    path: 'admin-dashboard',
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./admin-dashboard/admin-dashboard.component').then((m) => m.AdminDashboardComponent),
  },
  {
    path: 'requests',
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./requests/requests.component').then((m) => m.RequestsComponent),
  },
  {
    path: 'moderation',
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./moderation/moderation.component').then((m) => m.ModerationComponent),
  },
  {
    path: 'profile',
    canActivate: [AuthGuard],
    loadComponent: () => import('./profile/profile.component').then((m) => m.ProfileComponent),
  },
  {
    path: 'shops/:id',
    canActivate: [AuthGuard],
    loadComponent: () =>
      import('./shop-details/shop-page.component').then((m) => m.ShopPageComponent),
  },
  {
    path: 'error',
    loadComponent: () => import('./error/error.component').then((m) => m.ErrorComponent),
  },
  { path: '**', redirectTo: 'error' },
];
