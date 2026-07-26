# Kartyavya - Pre-Day-1 Package - START HERE

This zip contains everything frozen in your two source documents:
- `Kartyavya_Pre_Day_1_Technical_Contracts_v1_0.pdf`
- `Kartyavya_Team_2_Exclusive_Microservice_Seven_Day_Plan_v3_0.pdf`

It has two folders, matching the two Git repositories your contracts require:

```
Kartyavya-PreDay1/
├── kartyavya/            <- main repo (public, all 6 members)
└── kartyavya-config/     <- private repo (M1 / you only)
```

`kartyavya/` is a Maven multi-module reactor project: one root `pom.xml` with 7 modules
(config-server, service-registry, api-gateway, access-admin-service, civic-report-service,
ai-analytics-service, email-notification-service) plus a `frontend/` (React 19 + Vite) folder.
Everyone builds with the same command:

```
cd kartyavya
mvn clean install          # builds all 7 Spring modules in dependency order
```

or per-service, once you're inside your own folder:
```
mvn -f access-admin-service/pom.xml clean package
```

---

## STEP 0 - If you already pushed an older version, delete those repos first
On GitHub: `kartyavya` -> Settings -> scroll to bottom -> Delete this repository (type the name to
confirm). Repeat for `kartyavya-config`. Skip this step if you're pushing for the first time.
Note: this rewrites history, so if any teammate already cloned, tell them to delete their local
copy and re-clone fresh after Step 6 below - don't let them merge an old branch back in.

## STEP 1 - Unzip and inspect
```
unzip Kartyavya-PreDay1.zip
cd Kartyavya-PreDay1
```
Both `kartyavya/` and `kartyavya-config/` are already `git init`-ed locally with one initial commit
each, on branch `main` (kartyavya also has `develop` checked out). You do not need to run `git init` again.

## Config file format note
All Spring configuration in this project is `.properties`, not YAML - `kartyavya-config/*.properties`
(served by Config Server) and each service's own `src/main/resources/application.properties`
(spring.application.name, spring.config.import, spring.profiles.active). `infra/docker-compose.yml`
and `.github/workflows/ci.yml` stay YAML since that's a fixed requirement of those tools. See
`docs/contracts/config-contracts.md` for the full contract.

## STEP 2 - Read the frozen contracts (you, before anyone else touches anything)
Open, in this order:
1. `kartyavya/docs/contracts/architecture.md`
2. `kartyavya/docs/contracts/service-catalog.md`
3. `kartyavya/docs/contracts/member-ownership.md`
4. The rest of `kartyavya/docs/contracts/` (gateway-routes, api-contracts, feign-contracts,
   rabbitmq-events, database-schema, config-contracts)
5. `kartyavya/.agents/rules/kartyavya-project.md` - this is the file Antigravity (or any AI coding
   agent) reads automatically before it edits any code. It enforces the exclusive-folder ownership
   and the inspect -> plan -> approve -> implement -> test -> diff workflow from your seven-day plan.

## STEP 3 - Fill in real GitHub usernames
Open `kartyavya/.github/CODEOWNERS` and replace `@M1-username` ... `@M6-username` with your
teammates' actual GitHub handles.

## STEP 4 - Create two GitHub repositories
1. Go to github.com -> New repository -> name it `kartyavya` -> Public or Private (your team's
   choice) -> **do not** initialize with a README/gitignore (you already have one).
2. Create a second repository named `kartyavya-config` -> set it **Private** (it will later hold
   environment-variable placeholders and, eventually, real config references - keep it locked down).

## STEP 5 - Push `kartyavya`
```
cd Kartyavya-PreDay1/kartyavya
git remote add origin https://github.com/<your-org-or-username>/kartyavya.git
git push -u origin main
git push -u origin develop
```

## STEP 6 - Push `kartyavya-config`
```
cd ../kartyavya-config
git remote add origin https://github.com/<your-org-or-username>/kartyavya-config.git
git push -u origin main
```

## STEP 7 - Protect branches (on GitHub, for the `kartyavya` repo)
Settings -> Branches -> Add rule for `main` AND a separate rule for `develop`:
- Require a pull request before merging (>= 1 approval)
- Require status checks to pass before merging -> select `kartyavya-ci` (from `.github/workflows/ci.yml`)
- Require review from Code Owners (this activates your CODEOWNERS file)

## STEP 8 - Add your 5 teammates as collaborators
Settings -> Collaborators and teams -> Add people -> give Write access to both repos
(`kartyavya-config` only needs you, M1, but add the others as Read if you want them to see the
structure).

## STEP 9 - Local build sanity check (do this before Day 1 stand-up)
```
cd kartyavya
mvn clean install
cd frontend
npm ci && npm run build
```
If MySQL/Mongo/RabbitMQ aren't running yet, the plain `mvn clean install` / `npm run build` will
still succeed - it only compiles and packages, it doesn't need the databases up. To test the full
integrated stack:
```
docker compose -f infra/docker-compose.yml up --build
```

## STEP 10 - Onboard every member (Day 1, before the 09:15 stand-up)
Each member:
```
git clone https://github.com/<your-org-or-username>/kartyavya.git
cd kartyavya
git checkout develop
git checkout -b feature/M<N>-<short-task-name>
```
Then they open **only** their exclusive folder(s) (per `docs/contracts/member-ownership.md`) in
their editor / Antigravity, and follow the Day 1 tasks in the seven-day plan (already summarized
inside `docs/contracts/` and referenced by `.agents/rules/kartyavya-project.md`).

## STEP 11 - Antigravity per-ticket workflow (every member, every ticket, every day)
Use this as the literal first message to Antigravity (copy from
`kartyavya/.agents/rules/kartyavya-project.md`, Section 3, or from your seven-day-plan PDF Section
10.3):
```
You are implementing one Kartyavya ticket inside the current cloned repository.
Member: <NUMBER AND ROLE>
Exclusive service folder: <EXACT SERVICE FOLDER>
Owned frontend folder, if any: <EXACT FRONTEND FOLDER>
Ticket: <TITLE>
Acceptance criteria: <PASTE>

Before writing code:
1. Read .agents/rules/kartyavya-project.md and every file in docs/contracts.
2. Inspect Git branch/status, current implementation and tests.
3. Confirm the exact Spring application name and that no other service folder will be modified.
4. Check Config Server keys, Eureka registration, Gateway routes, Feign contracts and RabbitMQ events affected.
5. Produce a file-by-file plan with APIs, configuration, database/events, validation, tests and verification commands.
Do not modify files until I approve the plan.
```
Only after you review and approve the plan does the member send: `APPROVE IMPLEMENTATION`
(full text also in the rules file).

## STEP 12 - Daily rhythm (from your seven-day plan, unchanged)
09:15 stand-up -> 09:30 pull develop + branch -> 11:30 first build/registration check -> 13:00
integration sync (merge approved PRs) -> 14:00 pull merged develop -> 17:30 full docker-compose
test -> 19:00 demo + evidence upload.

## STEP 13 - Day-1 exit gate (must be true before Day 2 starts)
- Config Server (8888) serves application/profile config
- Eureka (8761) shows api-gateway, access-admin-service, civic-report-service,
  ai-analytics-service, email-notification-service all UP
- API Gateway (8080) health check passes and every lb:// route resolves
- React frontend shell builds and calls only the Gateway health endpoint
- Contract files in `docs/contracts/` are merged into `develop` with no fields marked TBD

---
Everything above mirrors exactly what is frozen in your two PDFs (Pre-Day-1 Technical Contracts v1.0
and the Exclusive Microservice Seven-Day Plan v3.0). If any of these steps conflict with what's in
`docs/contracts/`, the contracts win - update this file via a `contract/<short-description>` PR
before implementation branches use the new shape.
