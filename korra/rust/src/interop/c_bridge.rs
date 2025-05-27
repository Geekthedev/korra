//! FFI layer to C thread system

use std::ffi::{c_void, CStr, CString};
use std::os::raw::{c_char, c_int};
use std::slice;
use std::ptr;

use crate::engine::agent::Agent;

// Function to register Rust callbacks with C
pub fn register_callbacks() {
    // In a real implementation, this would register callbacks with the C code
    // For this demo, the callbacks are already defined in lib.rs
}

// Function to convert C strings to Rust strings
pub fn c_str_to_string(c_str: *const c_char) -> Result<String, &'static str> {
    if c_str.is_null() {
        return Err("Null pointer");
    }
    
    unsafe {
        let c_str = CStr::from_ptr(c_str);
        match c_str.to_str() {
            Ok(s) => Ok(s.to_string()),
            Err(_) => Err("Invalid UTF-8"),
        }
    }
}

// Function to convert Rust strings to C strings
pub fn string_to_c_str(s: &str) -> *mut c_char {
    match CString::new(s) {
        Ok(c_str) => c_str.into_raw(),
        Err(_) => ptr::null_mut(),
    }
}

// Function to free C strings allocated by Rust
pub fn free_c_str(c_str: *mut c_char) {
    if !c_str.is_null() {
        unsafe {
            let _ = CString::from_raw(c_str);
        }
    }
}

// Function to convert C byte array to Rust slice
pub unsafe fn c_bytes_to_slice<'a>(bytes: *const u8, len: usize) -> &'a [u8] {
    if bytes.is_null() {
        &[]
    } else {
        slice::from_raw_parts(bytes, len)
    }
}

// Function to allocate memory for C
pub fn alloc_for_c(size: usize) -> *mut u8 {
    let mut vec = Vec::with_capacity(size);
    vec.resize(size, 0);
    
    let ptr = vec.as_mut_ptr();
    std::mem::forget(vec);
    
    ptr
}

// Function to convert Agent to C handle
pub fn agent_to_handle(agent: Agent) -> *mut c_void {
    let boxed = Box::new(agent);
    Box::into_raw(boxed) as *mut c_void
}

// Function to convert C handle to Agent
pub unsafe fn handle_to_agent(handle: *mut c_void) -> Option<&'static mut Agent> {
    if handle.is_null() {
        None
    } else {
        Some(&mut *(handle as *mut Agent))
    }
}