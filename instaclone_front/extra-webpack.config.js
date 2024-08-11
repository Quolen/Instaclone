const webpack = require('webpack');

module.exports = {
  resolve: {
    fallback: {
      net: false,
      tls: false,
      fs: false,
    },
  },
};
