// import { Routes } from '@angular/router';
// import { LoginComponent } from './auth/login/login.component';
// import { RegisterComponent } from './auth/register/register.component';
// import { LandingComponent } from './landing/landing.component';
// import { AboutComponent } from './about/about.component';
// import { ShopkeeperRegistrationComponent } from './shopkeeper-registration/shopkeeper-registration.component';
// import { DashboardComponent } from './dashboard/dashboard.component';
// import { ErrorComponent } from './error/error.component';
// import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
// import { ShopDashboardComponent } from './shop-dashboard/shop-dashboard.component';
// import { FavoritesComponent } from './favorites/favorites.component';
// import { RequestsComponent } from './requests/requests.component';
// import { ProductsComponent } from './products/products.component';
// import { ReviewsComponent } from './reviews/reviews.component';

// export const routes: Routes = [
//   { path: '', component: LandingComponent }, // Default landing page
//   { path: 'login', component: LoginComponent }, // Login page
//   { path: 'register', component: RegisterComponent }, // Register page
//   {path:'about',component:AboutComponent},
//   {path:'shop-registration',component:ShopkeeperRegistrationComponent},
//   {path:'dashboard',component:DashboardComponent},
//   {path:'admin-dashboard',component:AdminDashboardComponent},
//   {path:'shop-dashboard',component:ShopDashboardComponent},
//   {path:'favorites',component:FavoritesComponent},
//   {path:'requests',component:RequestsComponent},
//   {path:'products',component:ProductsComponent},
//   {path:'reviews',component:ReviewsComponent},
//   {path:'error',component:ErrorComponent}
// ];

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

export const routes: Routes = [
  { path: '', component: LandingComponent }, 
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'about', component: AboutComponent },

  // Protected routes (User logged in)
  { path: 'shop-registration', component: ShopkeeperRegistrationComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard], data: { roles: ['USER'] } },
  { path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'shop-dashboard', component: ShopDashboardComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  { path: 'favorites', component: FavoritesComponent, canActivate: [AuthGuard], data: { roles: ['USER'] } },
  { path: 'requests', component: RequestsComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'products', component: ProductsComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  { path: 'reviews', component: ReviewsComponent, canActivate: [AuthGuard], data: { roles: ['SHOPKEEPER'] } },
  
  { path: 'error', component: ErrorComponent }
];
