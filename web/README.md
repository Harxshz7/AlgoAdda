# AlgoAdda - Web Frontend (`web`)

React 18 + Vite + TypeScript frontend for the AlgoAdda trading bot marketplace.

## Tech Stack
- **React 18**
- **TypeScript**
- **Vite**
- **Tailwind CSS v4**

---

## Getting Started

### 1. Configure Environment Variables
Copy `.env.example` to `.env` (or use the pre-configured default):
```bash
cp .env.example .env
```
Default configuration:
```env
VITE_API_URL=http://localhost:8080
```

### 2. Install Dependencies
```bash
cd web
npm install
```

### 3. Run Development Server
```bash
npm run dev
```

The frontend will run at `http://localhost:5173`.

---

## Build for Production

To create an optimized production build:
```bash
npm run build
```
Build output will be generated in `web/dist/`.
