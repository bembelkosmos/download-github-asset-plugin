# Important notes
 
## CI/CD
CI/CD workflow in short:

1. Do not commit on main branch! Create feature or bugfix branch.
2. Crete a changeset (if required) to describe mayor, minor or patches. A changeset bot will remind you.
3. After the pull request is created tests and quality checks will run.
4. Merge only if checks are successful!
5. The merge to main will create release pull request. It merges all changesets and creates a new sem-version, and it creates the artifacts (tbd).
6. Publishing the pull request creates new artifacts (tbd) and push it into registries. In addition, it could trigger an deployment to the server.   

### Prerequisites 
- Install node.js
- Configure npm changeset

### Install node.js
Download installer of https://nodejs.org/en/download/package-manager

### Configure changeset

1. Init changeset: npm install @changesets/cli && npx changeset init
2. Update ./changeset/config.json:
   - Add "privatePackages" to { version: true, tag: true }
   - Verify in ./package.json must be the attributes "name", "private" (true) and "version"
   - see: https://github.com/changesets/changesets/blob/main/docs/versioning-apps.md
3. Use changeset: "npx changeset" to add new changeset
4. Create a PAT:
   - PAT must be permissions to content and pull requests (read and write)
   - Store PAT in github secrets with name CHANGESETS_TOKEN

