import venue1 from '@/assets/venues/venue-1.jpg';
import venue2 from '@/assets/venues/venue-2.jpg';
import venue3 from '@/assets/venues/venue-3.jpg';

const imageMap: Record<number, string> = {
  1: venue1,
  2: venue2,
  3: venue3,
};

export const getVenueImage = (venueId?: number) => {
  if (!venueId) {
    return venue1;
  }
  return imageMap[venueId] || imageMap[(venueId % 3) + 1] || venue1;
};
