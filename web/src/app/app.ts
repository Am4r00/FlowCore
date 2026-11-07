import { Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AccountSummary } from './auth/account-summary';
import { AuthService} from './auth/auth.service';
import { OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms'
import { concatMap } from "rxjs";
import { tap } from "rxjs";
import { finalize } from "rxjs";


type SessionState =
  | { status: 'loading' }
  | { status: 'anonymous' }
  | { status: 'authenticated'; account: AccountSummary }
  | { status: 'error'; message: string };

@Component({
  imports: [RouterOutlet, FormsModule],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  session: SessionState = { status: 'loading' };
  private readonly authService = inject(AuthService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  username: string = '';
  password: string = '';
  feedbackMessage: string = '';
  pending: boolean = false;

  ngOnInit() {
    this.authService.getCurrentAccount().subscribe({
      next: (account) => {
        this.session = {
          status: 'authenticated',
          account: account,
        };
        this.changeDetector.markForCheck();
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.session = { status: 'anonymous' };
        } else {
          this.session = {
            status: 'error',
            message: 'Não foi possível verificar sua sessão. Tente novamente.',
          };
        }
        this.changeDetector.markForCheck();
      },
    });
  }
  submitLogin() {
      let stage: 'initialCsrf' | 'login' | 'renewCsrf' | 'account' = 'initialCsrf';
      if (this.pending) {
            return;
      }
      this.pending = true;
      this.feedbackMessage = "";

      this.authService
      .getCsrf()
      .pipe(
        concatMap((csrf) => {
          stage = 'login';
          return this.authService.login(this.username, this.password, csrf);
        }),
        concatMap(() => {
          stage = 'renewCsrf';
          return this.authService.getCsrf();
        }),
        concatMap(() => {
          stage = 'account';
          return this.authService.getCurrentAccount();
        }),
        finalize(() =>{
            this.pending = false ;
            this.changeDetector.markForCheck();
        })
      )
      .subscribe({
        next: (account) => {
          this.feedbackMessage = 'Login feito com sucesso !';
          this.session = {
            status: 'authenticated',
            account: account,
          };
          this.changeDetector.markForCheck();
        },
        error: (error: HttpErrorResponse) => {
          if (stage === 'login' && error.status === 401) {
            this.feedbackMessage = 'Não foi possivel fazer o login Usuário ou senha inválidos. ';
          } else if (stage === 'login' && error.status === 403) {
            this.feedbackMessage =
              'Não foi possível validar a solicitação de login. Tente novamente.';
          } else {
            this.feedbackMessage = 'Não foi possivel fazer o login tente novamente !';
          }
          this.changeDetector.markForCheck();
        },
      });
  }
  submitLogout() {
    let logoutConfirmed: boolean = false;

    if (this.pending) {
          return;
      }

    this.pending = true;
    this.feedbackMessage = "";

    this.authService
      .getCsrf()
      .pipe(
        concatMap((csrf) => this.authService.logout(csrf)),
        tap(() => {
          this.feedbackMessage = '';
          this.password = '';
          logoutConfirmed = true;
          this.session = { status: 'anonymous' };
          this.changeDetector.markForCheck();
        }),
        concatMap(() => this.authService.getCsrf()),
        finalize(() =>{
            this.pending = false;
            this.changeDetector.markForCheck();
        })
      )
      .subscribe({
        error: (error: HttpErrorResponse) => {
          this.feedbackMessage = logoutConfirmed
            ? 'A saída foi confirmada, mas não foi possível renovar o CSRF.'
            : 'Não foi possível confirmar que você saiu. Tente novamente.';
          this.changeDetector.markForCheck();
        },
      });
  }
}
