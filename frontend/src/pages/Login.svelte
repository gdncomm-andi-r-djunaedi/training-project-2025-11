<script>
  import { authApi } from '../lib/api/index.js';
  import { authStore, cartStore } from '../lib/stores/index.js';
  import { toastStore } from '../lib/stores/toast.js';
  import { navigate } from '../lib/router/index.js';

  let email = $state('');
  let password = $state('');
  let loading = $state(false);
  let error = $state(null);
  let showPassword = $state(false);

  async function handleSubmit(e) {
    e.preventDefault();
    loading = true;
    error = null;

    try {
      const tokenRes = await authApi.login({
        user: email,
        password: password
      });

      authStore.login({ id: tokenRes.user_id }, tokenRes.access_token);
      
      // Sync cart with server after login
      try {
        await cartStore.syncOnLogin();
      } catch (e) {
        console.error('Failed to sync cart:', e);
      }
      
      try {
        const userRes = await authApi.getProfile();
        authStore.updateUser(userRes);
      } catch (e) {
        console.error('Failed to get profile:', e);
      }

      toastStore.success('Welcome back!');
      
      const redirect = new URLSearchParams(window.location.search).get('redirect') || '/';
      navigate(redirect);
    } catch (e) {
      error = e.message || 'Invalid credentials';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Login - Waroenk</title>
</svelte:head>

<div class="min-h-screen flex items-center justify-center py-12 px-4 bg-[var(--color-bg)]">
  <div class="w-full max-w-sm">
    <!-- Logo -->
    <div class="text-center mb-6">
      <a href="/" class="inline-flex items-center gap-2">
        <div class="w-9 h-9 rounded-lg bg-[var(--color-primary)] flex items-center justify-center">
          <span class="text-white font-semibold">W</span>
        </div>
        <span class="text-xl font-semibold text-[var(--color-text)]">
          Waroenk
        </span>
      </a>
    </div>

    <!-- Form Card -->
    <div class="bg-white rounded-xl border border-[var(--color-border)] p-6">
      <h1 class="text-lg font-semibold text-center text-[var(--color-text)] mb-1">
        Welcome back
      </h1>
      <p class="text-xs text-[var(--color-text-muted)] text-center mb-6">
        Sign in to your account
      </p>

      {#if error}
        <div class="bg-red-50 text-[var(--color-error)] px-3 py-2 rounded-lg mb-4 text-xs">
          {error}
        </div>
      {/if}

      <form onsubmit={handleSubmit} class="space-y-4">
        <!-- Email -->
        <div>
          <label for="email" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Email or Phone
          </label>
          <input
            type="text"
            id="email"
            bind:value={email}
            placeholder="Enter your email or phone"
            required
            class="input"
          />
        </div>

        <!-- Password -->
        <div>
          <label for="password" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Password
          </label>
          <div class="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              bind:value={password}
              placeholder="Enter your password"
              required
              class="input pr-10"
            />
            <button 
              type="button"
              onclick={() => showPassword = !showPassword}
              class="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-muted)] hover:text-[var(--color-text)]"
              aria-label="Toggle password visibility"
            >
              {#if showPassword}
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              {:else}
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              {/if}
            </button>
          </div>
        </div>

        <!-- Forgot -->
        <div class="text-right">
          <a href="/forgot-password" class="text-xs text-[var(--color-primary)] hover:underline">
            Forgot password?
          </a>
        </div>

        <!-- Submit -->
        <button 
          type="submit"
          disabled={loading}
          class="btn btn-primary w-full py-2.5"
        >
          {#if loading}
            <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Signing in...
          {:else}
            Sign In
          {/if}
        </button>
      </form>

      <!-- Register Link -->
      <p class="text-center text-xs text-[var(--color-text-muted)] mt-4">
        Don't have an account?
        <a href="/register" class="text-[var(--color-primary)] font-medium hover:underline">
          Create one
        </a>
      </p>
    </div>
  </div>
</div>
