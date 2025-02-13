export default {
  dateToKrString: (date: Date) => {
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hour = date.getHours();
    const minute = date.getMinutes();

    return minute ? `${month}월 ${day}일 ${hour}시 ${minute}분` : `${month}월 ${day}일 ${hour}시 `;
  },

  dateToString: (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();

    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  },

  dateToHourAndMinute: (date: Date) => {
    const hour = String(date.getHours());
    const minute = String(date.getMinutes()).padStart(2, '0');

    return `${hour}:${minute}`;
  },

  getCurrentTimeInSeconds: (date: Date) => {
    const yearsInSeconds = date.getFullYear() * 365 * 24 * 60 * 60;
    const monthsInSeconds = date.getMonth() * 30 * 24 * 60 * 60;
    const daysInSeconds = date.getDate() * 24 * 60 * 60;
    const hoursInSeconds = date.getHours() * 60 * 60;
    const minutesInSeconds = date.getMinutes() * 60;
    const seconds = date.getSeconds();

    return yearsInSeconds + monthsInSeconds + daysInSeconds + hoursInSeconds + minutesInSeconds + seconds;
  },

  getTimeAndMinuteFromSeconds: (totalSeconds: number) => {
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return { hours, minutes, seconds };
  },
  timeToDecimalHours: (time: string) => {
    const [hours, minutes, seconds] = time.split(':').map(Number);
    return hours + minutes / 60 + seconds / 3600 - 8;
  },
  DateToDateData: (date: Date) => {
    const year = date.getFullYear();
    const month = date.getMonth() + 1; // getMonth()는 0부터 시작하므로 1을 더해줍니다.
    const day = date.getDate();
    const dateString = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
    const timestamp = date.getTime();

    return {
      dateString,
      day,
      month,
      timestamp,
      year,
    };
  },
};
