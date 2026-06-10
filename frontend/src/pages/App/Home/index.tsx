import { PageContainer, ProCard } from '@ant-design/pro-components';
import { Input, Card, Row, Col, message, Empty } from 'antd';
import React, { useEffect, useState } from 'react';
import { history } from '@umijs/max';
import { getVenues } from '@/services/app';
import { EnvironmentOutlined, PhoneOutlined, ClockCircleOutlined } from '@ant-design/icons';
import { getVenueImage } from '@/utils/venueImages';

const { Search } = Input;

const Home: React.FC = () => {
  const [venues, setVenues] = useState<API.Venue[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');

  // 加载球馆列表
  const loadVenues = async (searchKeyword?: string) => {
    setLoading(true);
    try {
      const response = await getVenues(searchKeyword);
      setVenues(response.data);
    } catch (error: any) {
      message.error(error.message || '加载失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVenues();
  }, []);

  // 搜索
  const handleSearch = (value: string) => {
    setKeyword(value);
    loadVenues(value);
  };

  // 进入球馆详情
  const handleVenueClick = (venueId: number) => {
    history.push(`/app/venues/${venueId}`);
  };

  return (
    <PageContainer
      title="球馆预约"
      subTitle="浏览真实球馆信息，快速选择合适场地"
    >
      <ProCard style={{ marginBottom: 16 }}>
        <Search
          placeholder="搜索球馆名称或地址"
          allowClear
          enterButton="搜索"
          size="large"
          data-testid="venue-search-input"
          onSearch={handleSearch}
          style={{ maxWidth: 600 }}
        />
      </ProCard>

      {venues.length === 0 && !loading ? (
        <Empty description={<span data-testid="venue-empty">暂无球馆</span>} />
      ) : (
        <Row gutter={[16, 16]}>
          {venues.map((venue) => (
            <Col xs={24} sm={12} md={8} lg={6} key={venue.id}>
              <Card
                hoverable
                loading={loading}
                style={{ borderRadius: 14, overflow: 'hidden' }}
                data-testid={`venue-card-${venue.id}`}
                onClick={() => handleVenueClick(venue.id)}
                cover={
                  <img
                    src={getVenueImage(venue.id)}
                    alt={venue.name}
                    className="venue-cover-image"
                  />
                }
              >
                <Card.Meta
                  title={venue.name}
                  description={
                    <div className="venue-card-meta">
                      <div className="venue-card-meta-item">
                        <EnvironmentOutlined /> {venue.address}
                      </div>
                      {venue.phone && (
                        <div className="venue-card-meta-item">
                          <PhoneOutlined /> {venue.phone}
                        </div>
                      )}
                      <div className="venue-card-meta-item">
                        <ClockCircleOutlined /> {venue.openTime} - {venue.closeTime}
                      </div>
                    </div>
                  }
                />
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </PageContainer>
  );
};

export default Home;
