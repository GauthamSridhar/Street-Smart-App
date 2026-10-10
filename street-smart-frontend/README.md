# Street Smart frontend

Start the backend first, then run `npm ci` and `npm start` here. Open
`http://localhost:4200`; the development proxy forwards `/api` to the gateway.

Discovery searches **products**, with combined category/availability filters and
server pagination. The map is optional; set `GOOGLE_MAPS_API_KEY` in the ignored
root `.env` file before starting the Compose lab. The container renders it into
`config.js` only at runtime. No backend secrets belong in browser configuration.

See [frontend design and API contracts](../docs/FRONTEND_DESIGN.md).
See [latest local verification](../docs/FINAL_CODE_VERIFICATION.md) for dashboard,
browser and test results. The [earlier frontend report](../docs/FRONTEND_VERIFICATION.md)
is a historical snapshot.

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 19.0.2.

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For browser acceptance against the isolated Docker stack, follow the setup in
[local verification](../docs/FINAL_CODE_VERIFICATION.md), then run here:

```bash
npm run test:visual
```

This repository provides its browser acceptance harness; `ng e2e` is not the
configured verification entry point.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
