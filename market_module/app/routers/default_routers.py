from flask import Blueprint, request, current_app
from flask_jwt_extended import jwt_required, create_access_token
from ..utils.response import api_response
from ..handler import default_handler

default_bp = Blueprint('market', __name__)

@default_bp.route('/get_klines', methods=['GET'])
def get_klines():
    platform = request.args.get('platform', default='OKX', type=str)
    symbol = request.args.get('symbol', default='BTC_USDT', type=str)
    market_type = request.args.get('market_type', default='SPOT', type=str)
    interval = request.args.get('interval', default='1m', type=str)
    start_time = request.args.get('start_time', default=0, type=int)
    end_time = request.args.get('end_time', default=0, type=int)
    limit = request.args.get('limit', default=1000, type=int)

    success, res, msg = default_handler.get_klines(platform, symbol, market_type, interval, start_time, end_time, limit)
    
    if success:
        return api_response(code=200, data=res, message=msg)
    else:
        return api_response(code=500, data=res, message=msg)


@default_bp.route('/get_ticker', methods=['GET'])
def get_ticker():
    platform = request.args.get('platform', default='OKX', type=str)
    symbol = request.args.get('symbol', default='BTC_USDT', type=str)
    success, res, msg = default_handler.get_ticker(platform, symbol)
    if success:
        return api_response(code=200, data=res, message=msg)
    else:
        return api_response(code=500, data=res, message=msg)


@default_bp.route('/get_depth', methods=['GET'])
def get_depth():
    platform = request.args.get('platform', default='OKX', type=str)
    symbol = request.args.get('symbol', default='BTC_USDT', type=str)
    success, res, msg = default_handler.get_depth(platform, symbol)
    if success:
        return api_response(code=200, data=res, message=msg)
    else:
        return api_response(code=500, data=res, message=msg)

@default_bp.route('/get_market', methods=['GET'])
def test():
    success, res, msg = default_handler.get_market()
    if success:
        return api_response(code=200, data=res, message=msg)
    else:
        return api_response(code=500, data=res, message=msg)

@default_bp.route('/echo_test', methods=['POST'])
def echo_test():
    req = request.get_json()
    return api_response(code=200, data=req, message="echo")