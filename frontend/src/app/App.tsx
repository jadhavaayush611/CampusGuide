import { RouterProvider } from "react-router";
import { router } from "./routes";
import { AppProviders } from "../core/providers/AppProviders";

export default function App() {
  return (
    <AppProviders>
      <RouterProvider router={router} />
    </AppProviders>
  );
}
