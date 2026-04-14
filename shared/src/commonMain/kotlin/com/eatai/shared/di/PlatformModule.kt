package com.eatai.shared.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin bindings (e.g. Settings, Context).
 * Each target provides its own [actual] implementation.
 * Initialized alongside [sharedModule] in each platform's entry point.
 */
expect fun platformModule(): Module
