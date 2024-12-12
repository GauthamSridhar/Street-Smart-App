import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { LandingComponent } from './landing/landing.component';
import { AboutComponent } from './about/about.component';
import { ShopkeeperRegistrationComponent } from './shopkeeper-registration/shopkeeper-registration.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ErrorComponent } from './error/error.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { ShopDashboardComponent } from './shop-dashboard/shop-dashboard.component';
import { FavoritesComponent } from './favorites/favorites.component';
import { RequestsComponent } from './requests/requests.component';
import { ProductsComponent } from './products/products.component';
import { ReviewsComponent } from './reviews/reviews.component';
import { AuthGuard } from './auth.guard'; // Ensure correct import path
import { ShopRejectedComponent } from './shop-rejected/shop-rejected.component';

export const routes: Routes = [
  { path: '', component: LandingComponent }, 
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'about', component: AboutComponent },
  
  // Protected routes (User logged in)
  {path:'shop-rejected',component:ShopRejectedComponent,canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] }},
  { path: 'shop-registration', component: ShopkeeperRegistrationComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard], data: { roles: ['USER'] } },
  { path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'shop-dashboard', component: ShopDashboardComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  { path: 'favorites', component: FavoritesComponent, canActivate: [AuthGuard], data: { roles: ['USER'] } },
  { path: 'requests', component: RequestsComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'products', component: ProductsComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  { path: 'reviews', component: ReviewsComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },

  // Wildcard route: if no match, redirect to the landing page (main page)
  { path: '**', redirectTo: '', pathMatch: 'full' },

  { path: 'error', component: ErrorComponent }
];
