<script>
  import { authApi } from '../lib/api/index.js';
  import { toastStore } from '../lib/stores/toast.js';
  import { navigate } from '../lib/router/index.js';

  let fullName = $state('');
  let email = $state('');
  let phone = $state('');
  let password = $state('');
  let confirmPassword = $state('');
  let loading = $state(false);
  let error = $state(null);
  let showPassword = $state(false);

  async function handleSubmit(e) {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      error = 'Passwords do not match';
      return;
    }
    
    if (password.length < 6) {
      error = 'Password must be at least 6 characters';
      return;
    }

    loading = true;
    error = null;

    try {
      await authApi.register({
        full_name: fullName,
        email: email,
        phone: phone,
        password: password
      });

      toastStore.success('Account created! Please login.');
      navigate('/login');
    } catch (e) {
      error = e.message || 'Registration failed';
    } finally {
      loading = false;
    }
  }
</script>

<svelte:head>
  <title>Register - Waroenk</title>
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
        Create Account
      </h1>
      <p class="text-xs text-[var(--color-text-muted)] text-center mb-6">
        Join Waroenk and start shopping
      </p>

      {#if error}
        <div class="bg-red-50 text-[var(--color-error)] px-3 py-2 rounded-lg mb-4 text-xs">
          {error}
        </div>
      {/if}

      <form onsubmit={handleSubmit} class="space-y-3">
        <div>
          <label for="fullName" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Full Name
          </label>
          <input
            type="text"
            id="fullName"
            bind:value={fullName}
            placeholder="Enter your full name"
            required
            class="input"
          />
        </div>

        <div>
          <label for="email" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Email
          </label>
          <input
            type="email"
            id="email"
            bind:value={email}
            placeholder="Enter your email"
            required
            class="input"
          />
        </div>

        <div>
          <label for="phone" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Phone
          </label>
          <input
            type="tel"
            id="phone"
            bind:value={phone}
            placeholder="Enter your phone"
            required
            class="input"
          />
        </div>

        <div>
          <label for="password" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Password
          </label>
          <div class="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              bind:value={password}
              placeholder="Create a password"
              required
              minlength="6"
              class="input pr-10"
            />
            <button 
              type="button"
              onclick={() => showPassword = !showPassword}
              class="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--color-text-muted)] hover:text-[var(--color-text)]"
              aria-label="Toggle password visibility"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                {#if showPassword}
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                {:else}
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                {/if}
              </svg>
            </button>
          </div>
        </div>

        <div>
          <label for="confirmPassword" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
            Confirm Password
          </label>
          <input
            type="password"
            id="confirmPassword"
            bind:value={confirmPassword}
            placeholder="Confirm your password"
            required
            class="input"
          />
        </div>

        <button 
          type="submit"
          disabled={loading}
          class="btn btn-primary w-full py-2.5 mt-2"
        >
          {#if loading}
            <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            Creating Account...
          {:else}
            Create Account
          {/if}
        </button>
      </form>

      <p class="text-center text-xs text-[var(--color-text-muted)] mt-4">
        Already have an account?
        <a href="/login" class="text-[var(--color-primary)] font-medium hover:underline">
          Sign in
        </a>
      </p>
    </div>
  </div>
</div>
