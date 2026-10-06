# Protección recomendada de `main`

Para cumplir el flujo donde nadie empuja directo a `main`, habilitar en GitHub:

1. **Require a pull request before merging**.
2. **Require status checks to pass before merging** y seleccionar `validate` (workflow `CI Checkstyle`).
3. **Block force pushes** y **block deletions**.
4. **Require review from Code Owners** (ya existe `CODEOWNERS`).

Con esto, cualquier cambio a `main` pasa por PR + CI con Checkstyle.
