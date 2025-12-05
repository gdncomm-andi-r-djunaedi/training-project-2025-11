<script>
  import { authApi } from '../lib/api/index.js';
  import { toastStore } from '../lib/stores/toast.js';
  import { navigate } from '../lib/router/index.js';

  let phoneOrEmail = $state('');
  let loading = $state(false);
  let error = $state(null);
  let success = $state(false);
  let resetToken = $state(null);

  async function handleSubmit(e) {
    e.preventDefault();
    loading = true;
    error = null;

    try {
      const response = await authApi.forgotPassword(phoneOrEmail);
      
      if (response.success) {
        success = true;
        // In a real app, the token would be sent via email/SMS
        // For development/testing, it might be returned in the response
        if (response.reset_token) {
          resetToken = response.reset_token;
        }
        toastStore.success('Password reset instructions sent!');
      } else {
        error = response.message || 'Failed to send reset instructions';
      }
    } catch (e) {
      error = e.message || 'Failed to send reset instructions';
    } finally {
      loading = false;
    }
  }

  function goToResetPassword() {
    if (resetToken) {
      navigate(`/reset-password?token=${resetToken}`);
    } else {
      navigate('/reset-password');
    }
  }
</script>

<svelte:head>
  <title>Forgot Password - Waroenk</title>
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
      {#if success}
        <!-- Success State -->
        <div class="text-center">
          <div class="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-7 h-7 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 class="text-lg font-semibold text-[var(--color-text)] mb-2">
            Check your inbox
          </h1>
          <p class="text-xs text-[var(--color-text-muted)] mb-6">
            We've sent password reset instructions to your email or phone.
          </p>
          
          {#if resetToken}
            <!-- Development: Show reset link directly -->
            <div class="bg-blue-50 text-blue-700 px-3 py-2 rounded-lg mb-4 text-xs">
              <strong>Dev Mode:</strong> Reset token available
            </div>
            <button
              onclick={goToResetPassword}
              class="btn btn-primary w-full py-2.5 mb-3"
            >
              Reset Password Now
            </button>
          {/if}

          <a href="/login" class="text-xs text-[var(--color-primary)] hover:underline">
            Back to Login
          </a>
        </div>
      {:else}
        <!-- Form State -->
        <h1 class="text-lg font-semibold text-center text-[var(--color-text)] mb-1">
          Forgot password?
        </h1>
        <p class="text-xs text-[var(--color-text-muted)] text-center mb-6">
          Enter your email or phone number and we'll send you reset instructions.
        </p>

        {#if error}
          <div class="bg-red-50 text-[var(--color-error)] px-3 py-2 rounded-lg mb-4 text-xs">
            {error}
          </div>
        {/if}

        <form onsubmit={handleSubmit} class="space-y-4">
          <!-- Email or Phone -->
          <div>
            <label for="phoneOrEmail" class="block text-xs font-medium text-[var(--color-text)] mb-1.5">
              Email or Phone Number
            </label>
            <input
              type="text"
              id="phoneOrEmail"
              bind:value={phoneOrEmail}
              placeholder="Enter your email or phone"
              required
              class="input"
            />
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
              Sending...
            {:else}
              Send Reset Instructions
            {/if}
          </button>
        </form>

        <!-- Back to Login -->
        <p class="text-center text-xs text-[var(--color-text-muted)] mt-4">
          Remember your password?
          <a href="/login" class="text-[var(--color-primary)] font-medium hover:underline">
            Sign in
          </a>
        </p>
      {/if}
    </div>
  </div>
</div>



