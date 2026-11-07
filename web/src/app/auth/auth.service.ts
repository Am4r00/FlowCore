import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { AccountSummary } from './account-summary';
import { CsrfResponse } from './csrf-response';
import { Observable } from "rxjs";

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  getCurrentAccount() {
    return this.http.get<AccountSummary>('/api/auth/me');
  }

  getCsrf() {
    return this.http.get<CsrfResponse>('/api/auth/csrf');
  }

  login(
      username: string,
      password: string,
      csrf: CsrfResponse
  ): Observable<void> {
      const body = new HttpParams()
          .set('username', username )
          .set("password", password);
      const header = new HttpHeaders()
          .set(csrf.headerName, csrf.token);

    return this.http.post<void>("/api/auth/login", body, {headers: header});
  }

  logout(csrf: CsrfResponse): Observable<void>{
    const header = new HttpHeaders()
        .set(csrf.headerName,csrf.token);

    return this.http.post<void>('/api/auth/logout',null,{headers: header});
  }
}
