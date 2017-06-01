# Logging in Boot Reproduction Case

This repository reproduces issues seen in projects where boot-logservice and
boot-http seem to be fundamentally incompatible.

As it turns out, boot-http uses a worker [Pod][pods] which does not share the
same memory space as the main Pod. As a result, everything but `serve`'s
`:init` key has no effect on the worker pod.

## Usage

See build.boot for a number of `serve-*` tasks. Each of these is a sample of different behavior.

- `serve-pod` - boot-http (worker pod) with no `:init`
- `serve-main` - raw run-jetty (main pod) **works**

| task         | works? | STDOUT | logs  | notes |
|:------------:|:------:|:------:|:-----:|:------|
| `serve-pod`  | ✘      | ✔      | ✘     | |
| `serve-pod-init` | ✘  | ✘      | ✘     | Fails to launch, can't find boot/core.clj |
| `serve-main` | ✔      | ✔      | ✔     | |

To examine a test case:

1. Launch the server with `boot <serve-* task>`
2. Note the output of `boot`

   You should see an entries for "Starting server" and "Base Logger: "

3. Note any files in `logs/`

   When properly configured, a log call should bottom out in logs/ as well.

4. Run `curl localhost:3333`

   You should see both a "log-factory" of `#object[adzerk.boot_logservice...]`
   and "In respond-hello" in **both STDOUT and logs/<today>.log**

## License

Copyright © 2017 Ryan Neufeld

[pods]: https://github.com/boot-clj/boot/wiki/Pods
