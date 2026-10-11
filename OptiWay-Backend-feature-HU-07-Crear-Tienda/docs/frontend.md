```js
    const { data, error } = await supabase.auth.signInWithPassword({
    email: email,
    password: password
    });
```