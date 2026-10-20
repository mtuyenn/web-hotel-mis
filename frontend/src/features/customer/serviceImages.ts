import water from "./assets/minibar-water.jpg";
import juice from "./assets/minibar-juice.jpg";
import beer from "./assets/minibar-beer.jpg";
import cashews from "./assets/minibar-cashews.jpg";
import placeholder from "./assets/service-placeholder.svg";

const demoImages: Record<string, string> = {
  OPSWATER: water,
  OPSJUICE: juice,
  OPSBEER: beer,
  OPSSNACK: cashews,
};

/** Bundled illustrations for demo goods whose catalog records have no photo. */
export const serviceImageSource = (serviceId: string, imageUrl: string | null) =>
  imageUrl?.trim() || demoImages[serviceId] || placeholder;

export const serviceImagePlaceholder = placeholder;
