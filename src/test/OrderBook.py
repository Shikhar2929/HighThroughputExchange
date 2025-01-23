from collections import OrderedDict

class OrderBook:
    def __init__(self, raw_order_book: dict = None):
        if raw_order_book is None:
            raw_order_book = {}
        if not isinstance(raw_order_book, dict):
            raise TypeError("Input data must be a dictionary.")

        self.order_books = {}
        for ticker, volumes in raw_order_book.items():
            self.order_books[ticker] = {
                'bidVolumes': self._create_sorted_dict(volumes.get('bidVolumes', {}), reverse=True),
                'askVolumes': self._create_sorted_dict(volumes.get('askVolumes', {}), reverse=False)
            }

    def _create_sorted_dict(self, volumes: dict, reverse: bool):
        return OrderedDict(sorted(
            ((float(price), float(qty)) for price, qty in volumes.items()),
            key=lambda x: x[0],
            reverse=reverse
        ))

    def update_volumes(self, updates: list):
        if not isinstance(updates, list):
            raise TypeError("Updates must be provided as a list.")

        for update in updates:
            if not isinstance(update, dict) or \
               'ticker' not in update or 'price' not in update or 'side' not in update or 'volume' not in update:
                raise ValueError("Each update must be a dictionary with keys 'ticker', 'price', 'side', and 'volume'.")

            ticker = update['ticker']
            price = float(update['price'])
            side = update['side'].upper()
            volume = float(update['volume'])

            if ticker not in self.order_books:
                self.order_books[ticker] = {
                    'bidVolumes': OrderedDict(),
                    'askVolumes': OrderedDict()
                }

            if side == 'BID':
                if volume == 0.0:
                    self.order_books[ticker]['bidVolumes'].pop(price, None)
                else:
                    self.order_books[ticker]['bidVolumes'][price] = volume
                    self.order_books[ticker]['bidVolumes'] = self._create_sorted_dict(self.order_books[ticker]['bidVolumes'], reverse=True)
            elif side == 'ASK':
                if volume == 0.0:
                    self.order_books[ticker]['askVolumes'].pop(price, None)
                else:
                    self.order_books[ticker]['askVolumes'][price] = volume
                    self.order_books[ticker]['askVolumes'] = self._create_sorted_dict(self.order_books[ticker]['askVolumes'], reverse=False)
            else:
                raise ValueError("Side must be 'BID' or 'ASK'.")

    def __repr__(self):
        """
        Return a string representation of the order book.

        Returns:
            str: A string representation of the order books for all tickers.
        """
        return f"OrderBook({self.order_books})"
